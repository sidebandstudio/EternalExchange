package moze_intel.projecte.network;

/**
 * Fabric port: upstream used NeoForge's VersionChecker against update.json; Fabric has no built-in version checker, so this is a no-op kept for call-site
 * compatibility.
 */
public class ThreadCheckUpdate extends Thread {

	public ThreadCheckUpdate() {
		super("ProjectE Update Checker");
		setDaemon(true);
	}

	@Override
	public void run() {
	}
}
