package iKguana.nametag;

import java.util.ArrayList;
import java.util.HashMap;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import iKguana.profiler.Profiler;

public class NameTagAPI {
	private static NameTagAPI $instance = null;
	private static String name = "NameTag";
	public Config config;
	public HashMap<String, Config> configs;

	public NameTagAPI(NameTagPlugin plugin) {
		if ($instance != null)
			return;

		$instance = this;

		plugin.saveResource("config.yml", false);
		config = plugin.getConfig();
	}

	public String getTag(String player) {
		return Profiler.getInstance().open(player).getString(p("Tag"), config.getString("default-tag"));
	}

	public ArrayList<String> getTags(String player) {
		ArrayList<String> arr = (ArrayList<String>) Profiler.getInstance().open(player).getStringList(p("Tags"));
		return arr;
	}

	public void setTag(String player, String tag) {
		Profiler.getInstance().open(player).set(p("Tag"), tag);
		refresh(player);
	}

	public void addTag(String player, String tag) {
		ArrayList<String> arr = getTags(player);
		arr.add(tag);
		Profiler.getInstance().open(player).set(p("Tags"), arr);
	}

	public void removeTag(String player, String tag) {
		ArrayList<String> arr = getTags(player);
		arr.remove(arr.indexOf(tag));
		Profiler.getInstance().open(player).set(p("Tags"), arr);
	}

	public void removeAllTag(String player) {
		Profiler.getInstance().open(player).remove(p("Tag"));
		Profiler.getInstance().open(player).remove(p("Tags"));
		Profiler.getInstance().open(player).reload();
		refresh(player);
	}

	public void refresh(String pl) {
		Player player = Server.getInstance().getPlayer(pl);
		if (player == null)
			return;

		player.setNameTag(config.getString("nametag-format").replace("$TAG", getTag(pl)).replace("$PLAYER", pl));
	}

	public String p(String str) {
		return name + "." + str;
	}

	public static NameTagAPI getInstance() {
		return $instance;
	}
}
