package iKguana.nametag;

import cn.nukkit.plugin.PluginBase;

public class NameTagPlugin extends PluginBase {
	public void onEnable() {
		new NameTagAPI(this);
		getServer().getPluginManager().registerEvents(new NameTag(this), this);
	}
}
