/*
 * Copyright 2011-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.hopestarter.wallet;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.VersionMessage;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.store.WalletProtobufSerializer;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.WalletFiles;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.StagingApi;
import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

import org.hopestarter.wallet.service.BlockchainService;
import org.hopestarter.wallet.service.BlockchainServiceImpl;
import org.hopestarter.wallet.util.CrashReporter;
import org.hopestarter.wallet.util.Io;

/**
 * @author Andreas Schildbach
 */
public class WalletApplication extends Application
{
	private Configuration mConfig;
	private ActivityManager mActivityManager;

	private Intent mBlockchainServiceIntent;
	private Intent mBlockchainServiceCancelCoinsReceivedIntent;
	private Intent mBlockchainServiceResetBlockchainIntent;

	private File mWalletFile;
	private Wallet mWallet;
	private PackageInfo mPackageInfo;
	private StagingApi mStagingApi;
	private ServerApi mServerApi;

	public static final String ACTION_WALLET_REFERENCE_CHANGED = WalletApplication.class.getPackage().getName() + ".wallet_reference_changed";

	public static final int VERSION_CODE_SHOW_BACKUP_REMINDER = 205;

	private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);
    private Tracker mTracker;


    @Override
	public void onCreate()
	{
		new LinuxSecureRandom(); // init proper random number generator

		initLogging();

		getDefaultTracker();

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().permitDiskReads().permitDiskWrites().penaltyLog().build());

		Threading.throwOnLockCycles();

		log.info("=== starting app using configuration: {}, {}", Constants.TEST ? "test" : "prod", Constants.NETWORK_PARAMETERS.getId());

		super.onCreate();

		mPackageInfo = packageInfoFromContext(this);

		CrashReporter.init(getCacheDir());

		Threading.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(final Thread thread, final Throwable throwable)
			{
				log.info("bitcoinj uncaught exception", throwable);
				CrashReporter.saveBackgroundTrace(throwable, mPackageInfo);
			}
		};

		initMnemonicCode();

		initServerApi();

		mConfig = new Configuration(PreferenceManager.getDefaultSharedPreferences(this), getResources());
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		mBlockchainServiceIntent = new Intent(this, BlockchainServiceImpl.class);
		mBlockchainServiceCancelCoinsReceivedIntent = new Intent(BlockchainService.ACTION_CANCEL_COINS_RECEIVED, null, this,
				BlockchainServiceImpl.class);
		mBlockchainServiceResetBlockchainIntent = new Intent(BlockchainService.ACTION_RESET_BLOCKCHAIN, null, this, BlockchainServiceImpl.class);

		mWalletFile = getFileStreamPath(Constants.Files.WALLET_FILENAME_PROTOBUF);

		loadWalletFromProtobuf();

		if (mConfig.versionCodeCrossed(mPackageInfo.versionCode, VERSION_CODE_SHOW_BACKUP_REMINDER) && !mWallet.getImportedKeys().isEmpty())
		{
			log.info("showing backup reminder once, because of imported keys being present");
			mConfig.armBackupReminder();
		}

		mConfig.updateLastVersionCode(mPackageInfo.versionCode);

		afterLoadWallet();

		cleanupFiles();
	}

	private void initServerApi() {
		mServerApi = new ServerApi(this);
		mStagingApi = new StagingApi();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	private void afterLoadWallet()
	{
		mWallet.autosaveToFile(mWalletFile, 10, TimeUnit.SECONDS, new WalletAutosaveEventListener());

		// clean up spam
		mWallet.cleanup();

		migrateBackup();
	}

	private void initLogging()
	{
		final File logDir = getDir("log", Constants.TEST ? Context.MODE_WORLD_READABLE : MODE_PRIVATE);
		final File logFile = new File(logDir, "wallet.log");

		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
		filePattern.setContext(context);
		filePattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %logger{0} - %msg%n");
		filePattern.start();

		final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
		fileAppender.setContext(context);
		fileAppender.setFile(logFile.getAbsolutePath());

		final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
		rollingPolicy.setContext(context);
		rollingPolicy.setParent(fileAppender);
		rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d.log.gz");
		rollingPolicy.setMaxHistory(7);
		rollingPolicy.start();

		fileAppender.setEncoder(filePattern);
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.start();

		final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
		logcatTagPattern.setContext(context);
		logcatTagPattern.setPattern("%logger{0}");
		logcatTagPattern.start();

		final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
		logcatPattern.setContext(context);
		logcatPattern.setPattern("[%thread] %msg%n");
		logcatPattern.start();

		final LogcatAppender logcatAppender = new LogcatAppender();
		logcatAppender.setContext(context);
		logcatAppender.setTagEncoder(logcatTagPattern);
		logcatAppender.setEncoder(logcatPattern);
		logcatAppender.start();

		final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
		log.addAppender(fileAppender);
		log.addAppender(logcatAppender);
		log.setLevel(Level.INFO);
	}

	private static final String BIP39_WORDLIST_FILENAME = "bip39-wordlist.txt";

	private void initMnemonicCode()
	{
		try
		{
			final long start = System.currentTimeMillis();
			MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open(BIP39_WORDLIST_FILENAME), null);
			log.info("BIP39 wordlist loaded from: '" + BIP39_WORDLIST_FILENAME + "', took " + (System.currentTimeMillis() - start) + "ms");
		}
		catch (final IOException x)
		{
			throw new Error(x);
		}
	}

	public StagingApi getStagingApi() {
		return mStagingApi;
	}

	public ServerApi getServerApi() {
		return mServerApi;
	}

	private static final class WalletAutosaveEventListener implements WalletFiles.Listener
	{
		@Override
		public void onBeforeAutoSave(final File file)
		{
		}

		@Override
		public void onAfterAutoSave(final File file)
		{
			// make wallets world accessible in test mode
			if (Constants.TEST)
				Io.chmod(file, 0777);
		}
	}

	public Configuration getConfiguration()
	{
		return mConfig;
	}

	public Wallet getWallet()
	{
		return mWallet;
	}

	private void loadWalletFromProtobuf()
	{
		if (mWalletFile.exists())
		{
			final long start = System.currentTimeMillis();

			FileInputStream walletStream = null;

			try
			{
				walletStream = new FileInputStream(mWalletFile);

				mWallet = new WalletProtobufSerializer().readWallet(walletStream);

				if (!mWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
					throw new UnreadableWalletException("bad wallet network parameters: " + mWallet.getParams().getId());

				log.info("wallet loaded from: '" + mWalletFile + "', took " + (System.currentTimeMillis() - start) + "ms");
			}
			catch (final FileNotFoundException x)
			{
				log.error("problem loading wallet", x);

				Toast.makeText(WalletApplication.this, x.getClass().getName(), Toast.LENGTH_LONG).show();

				mWallet = restoreWalletFromBackup();
			}
			catch (final UnreadableWalletException x)
			{
				log.error("problem loading wallet", x);

				Toast.makeText(WalletApplication.this, x.getClass().getName(), Toast.LENGTH_LONG).show();

				mWallet = restoreWalletFromBackup();
			}
			finally
			{
				if (walletStream != null)
				{
					try
					{
						walletStream.close();
					}
					catch (final IOException x)
					{
						// swallow
					}
				}
			}

			if (!mWallet.isConsistent())
			{
				Toast.makeText(this, "inconsistent wallet: " + mWalletFile, Toast.LENGTH_LONG).show();

				mWallet = restoreWalletFromBackup();
			}

			if (!mWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
				throw new Error("bad wallet network parameters: " + mWallet.getParams().getId());
		}
		else
		{
			mWallet = new Wallet(Constants.NETWORK_PARAMETERS);

			backupWallet();

			mConfig.armBackupReminder();

			log.info("new wallet created");
		}
	}

	private Wallet restoreWalletFromBackup()
	{
		InputStream is = null;

		try
		{
			is = openFileInput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF);

			final Wallet wallet = new WalletProtobufSerializer().readWallet(is);

			if (!wallet.isConsistent())
				throw new Error("inconsistent backup");

			resetBlockchain();

			Toast.makeText(this, R.string.toast_wallet_reset, Toast.LENGTH_LONG).show();

			log.info("wallet restored from backup: '" + Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + "'");

			return wallet;
		}
		catch (final IOException x)
		{
			throw new Error("cannot read backup", x);
		}
		catch (final UnreadableWalletException x)
		{
			throw new Error("cannot read backup", x);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (final IOException x)
			{
				// swallow
			}
		}
	}

	public void saveWallet()
	{
		try
		{
			protobufSerializeWallet(mWallet);
		}
		catch (final IOException x)
		{
			throw new RuntimeException(x);
		}
	}

	private void protobufSerializeWallet(final Wallet wallet) throws IOException
	{
		final long start = System.currentTimeMillis();

		wallet.saveToFile(mWalletFile);

		// make wallets world accessible in test mode
		if (Constants.TEST)
			Io.chmod(mWalletFile, 0777);

		log.debug("wallet saved to: '" + mWalletFile + "', took " + (System.currentTimeMillis() - start) + "ms");
	}

	public void backupWallet()
	{
		final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(mWallet).toBuilder();

		// strip redundant
		builder.clearTransaction();
		builder.clearLastSeenBlockHash();
		builder.setLastSeenBlockHeight(-1);
		builder.clearLastSeenBlockTimeSecs();
		final Protos.Wallet walletProto = builder.build();

		OutputStream os = null;

		try
		{
			os = openFileOutput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF, Context.MODE_PRIVATE);
			walletProto.writeTo(os);
		}
		catch (final IOException x)
		{
			log.error("problem writing key backup", x);
		}
		finally
		{
			try
			{
				os.close();
			}
			catch (final IOException x)
			{
				// swallow
			}
		}
	}

	private void migrateBackup()
	{
		if (!getFileStreamPath(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF).exists())
		{
			log.info("migrating automatic backup to protobuf");

			// make sure there is at least one recent backup
			backupWallet();
		}
	}

	private void cleanupFiles()
	{
		for (final String filename : fileList())
		{
			if (filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_BASE58)
					|| filename.startsWith(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.') || filename.endsWith(".tmp"))
			{
				final File file = new File(getFilesDir(), filename);
				log.info("removing obsolete file: '{}'", file);
				file.delete();
			}
		}
	}

	public void startBlockchainService(final boolean cancelCoinsReceived)
	{
		if (cancelCoinsReceived)
			startService(mBlockchainServiceCancelCoinsReceivedIntent);
		else
			startService(mBlockchainServiceIntent);
	}

	public void stopBlockchainService()
	{
		stopService(mBlockchainServiceIntent);
	}

	public void resetBlockchain()
	{
		// implicitly stops blockchain service
		startService(mBlockchainServiceResetBlockchainIntent);
	}

	public void replaceWallet(final Wallet newWallet)
	{
		resetBlockchain();
		mWallet.shutdownAutosaveAndWait();

		mWallet = newWallet;
		mConfig.maybeIncrementBestChainHeightEver(newWallet.getLastBlockSeenHeight());
		afterLoadWallet();

		final Intent broadcast = new Intent(ACTION_WALLET_REFERENCE_CHANGED);
		broadcast.setPackage(getPackageName());
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	public void processDirectTransaction(final Transaction tx) throws VerificationException
	{
		if (mWallet.isTransactionRelevant(tx))
		{
			mWallet.receivePending(tx, null);
			broadcastTransaction(tx);
		}
	}

	public void broadcastTransaction(final Transaction tx)
	{
		final Intent intent = new Intent(BlockchainService.ACTION_BROADCAST_TRANSACTION, null, this, BlockchainServiceImpl.class);
		intent.putExtra(BlockchainService.ACTION_BROADCAST_TRANSACTION_HASH, tx.getHash().getBytes());
		startService(intent);
	}

	public static PackageInfo packageInfoFromContext(final Context context)
	{
		try
		{
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		}
		catch (final NameNotFoundException x)
		{
			throw new RuntimeException(x);
		}
	}

	public PackageInfo packageInfo()
	{
		return mPackageInfo;
	}

	public final String applicationPackageFlavor()
	{
		final String packageName = getPackageName();
		final int index = packageName.lastIndexOf('_');

		if (index != -1)
			return packageName.substring(index + 1);
		else
			return null;
	}

	public static String httpUserAgent(final String versionName)
	{
		final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
		versionMessage.appendToSubVer(Constants.USER_AGENT, versionName, null);
		return versionMessage.subVer;
	}

	public String httpUserAgent()
	{
		return httpUserAgent(packageInfo().versionName);
	}

	public int maxConnectedPeers()
	{
		final int memoryClass = mActivityManager.getMemoryClass();
		if (memoryClass <= Constants.MEMORY_CLASS_LOWEND)
			return 4;
		else
			return 6;
	}

	public static void scheduleStartBlockchainService(final Context context)
	{
		final Configuration config = new Configuration(PreferenceManager.getDefaultSharedPreferences(context), context.getResources());
		final long lastUsedAgo = config.getLastUsedAgo();

		// apply some backoff
		final long alarmInterval;
		if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_JUST_MS)
			alarmInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
		else if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_RECENTLY_MS)
			alarmInterval = AlarmManager.INTERVAL_HALF_DAY;
		else
			alarmInterval = AlarmManager.INTERVAL_DAY;

		log.info("last used {} minutes ago, rescheduling blockchain sync in roughly {} minutes", lastUsedAgo / DateUtils.MINUTE_IN_MILLIS,
				alarmInterval / DateUtils.MINUTE_IN_MILLIS);

		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent alarmIntent = PendingIntent.getService(context, 0, new Intent(context, BlockchainServiceImpl.class), 0);
		alarmManager.cancel(alarmIntent);

		// workaround for no inexact set() before KitKat
		final long now = System.currentTimeMillis();
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now + alarmInterval, AlarmManager.INTERVAL_DAY, alarmIntent);
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
		}
		return mTracker;
	}
}
