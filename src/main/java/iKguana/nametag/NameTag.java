package iKguana.nametag;

import java.util.ArrayList;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import iKguana.simpledialog.SimpleDialog;

public class NameTag implements Listener, CommandExecutor {
	public NameTag(NameTagPlugin plugin) {
		PluginCommand<NameTagPlugin> cmd = new PluginCommand<NameTagPlugin>("tag", plugin);
		cmd.setUsage("/tag");
		cmd.setDescription("칭호 관리");
		cmd.setExecutor(this);
		plugin.getServer().getCommandMap().register("NameTag", cmd);
	}

	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		NameTagAPI.getInstance().refresh(event.getPlayer().getName());
	}

	@EventHandler
	public void playerChatEvent(PlayerChatEvent event) {
		NameTagAPI api = NameTagAPI.getInstance();
		String format = api.config.getString("chat-format");
		format = format.replace("$TAG", api.getTag(event.getPlayer().getName()));
		format = format.replace("$PLAYER", event.getPlayer().getName());
		format = format.replace("$MESSAGE", event.getMessage());
		event.setFormat(format);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getLabel().equals("tag")) {
			if (sender.isOp() && args.length > 0 && !args[0].equalsIgnoreCase("user")) {
				if (sender.isOp() && sender.isPlayer()) {
					switch (args[0]) {
					case "add":
						if (args.length > 2) {
							if (Server.getInstance().getPlayerExact(args[1]) instanceof Player) {
								NameTagAPI.getInstance().addTag(args[1], args[2]);
								sender.sendMessage(Server.getInstance().getPlayer(args[1]).getName() + "님에게 " + args[2] + "칭호를 주었습니다.");
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag add <플레이어> <칭호>");
						break;
					case "set":
						if (args.length > 2) {
							if (Server.getInstance().getPlayerExact(args[1]) instanceof Player) {
								if (isInteger(args[2])) {
									int idx = Integer.parseInt(args[2]);
									ArrayList<String> arr = NameTagAPI.getInstance().getTags(args[1]);
									if (0 < idx && idx < arr.size()) {
										NameTagAPI.getInstance().setTag(args[1], arr.get(idx));
										sender.sendMessage(args[1].toLowerCase() + "님의 칭호를 " + arr.get(idx) + "로 변경하였습니다.");
									} else
										sender.sendMessage("존재하지 않는 색인번호입니다.");
								} else
									sender.sendMessage("색인번호 형식이 올바르지 않습니다.");
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag set <플레이어> <색인번호>");
						break;
					case "setforce":
						if (args.length > 2) {
							if (Server.getInstance().getPlayer(args[1]) instanceof Player) {
								NameTagAPI.getInstance().setTag(args[1], args[2]);
								sender.sendMessage(args[1].toLowerCase() + "님의 칭호를 " + args[2] + "로 변경하였습니다.");
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag setforce <플레이어> <칭호>");
						break;
					case "list":
						if (args.length > 1) {
							if (Server.getInstance().getPlayer(args[1]) instanceof Player) {
								String text = "현재 사용중인 칭호: " + NameTagAPI.getInstance().getTag(args[1]) + "\n";
								ArrayList<String> arr = NameTagAPI.getInstance().getTags(args[1]);
								if (arr.size() > 0) {
									text += "보유중인 칭호 목록\n";
									for (int i = 0; i < arr.size(); i++)
										text += i + ":" + arr.get(i) + "\n";
								} else
									text += "보유중인 칭호가 없습니다.";
								sender.sendMessage(text);
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag list <플레이어>");
						break;
					case "remove":
						if (args.length > 2) {
							if (Server.getInstance().getPlayerExact(args[1]) instanceof Player) {
								if (isInteger(args[2])) {
									int idx = Integer.parseInt(args[2]);
									ArrayList<String> arr = NameTagAPI.getInstance().getTags(args[1]);
									if (0 < idx && idx < arr.size()) {
										sender.sendMessage(args[1].toLowerCase() + "님의 칭호 " + arr.get(idx) + "(을)를 제거합니다.");
										NameTagAPI.getInstance().removeTag(args[1], arr.get(idx));
									} else
										sender.sendMessage("존재하지 않는 색인번호입니다.");
								} else
									sender.sendMessage("색인번호 형식이 올바르지 않습니다.");
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag set <플레이어> <색인번호>");
						break;
					case "reset":
						if (args.length > 1) {
							if (Server.getInstance().getPlayerExact(args[1]) instanceof Player) {
								NameTagAPI.getInstance().removeAllTag(args[1]);
								sender.sendMessage(args[1].toLowerCase() + "님의 모든 칭호를 초기화했습니다.");
							} else
								sender.sendMessage("존재하지 않는 플레이어입니다.");
						} else
							sender.sendMessage("/tag reset <플레이어>");
						break;
					default:
						sender.sendMessage("/tag <add|list|remove> [args...]");
					}
				} else
					onCommand(sender, cmd, label, new String[] {});
			} else {
				if (!sender.isOp() || (args.length > 0 && args[0].equalsIgnoreCase("user"))) {
					FormWindowSimple window = new FormWindowSimple("칭호 관리", "원하는 메뉴를 선택해주세요.");
					window.addButton(new ElementButton("칭호 확인"));
					window.addButton(new ElementButton("칭호 변경"));
					window.addButton(new ElementButton("칭호 삭제"));
					SimpleDialog.sendDialog(this, "form_mainMenu", (Player) sender, window, sender.getName());
				} else
					SimpleDialog.sendDialog(this, "form_input_name", (Player) sender, SimpleDialog.Type.FILTERING);
			}
		}
		return true;

	}

	public void form_input_name(PlayerFormRespondedEvent event, Object data) {
		String text = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();
		text = Server.getInstance().getPlayer(text).getName();
		FormWindowSimple window = new FormWindowSimple("칭호 관리 - " + text, "원하는 메뉴를 선택해주세요.");
		window.addButton(new ElementButton("칭호 확인"));
		window.addButton(new ElementButton("칭호 추가"));
		window.addButton(new ElementButton("칭호 변경"));
		window.addButton(new ElementButton("칭호 변경 (OP)"));
		window.addButton(new ElementButton("칭호 삭제"));
		window.addButton(new ElementButton("칭호 초기화"));
		SimpleDialog.sendDialog(this, "form_mainMenu", event.getPlayer(), window, text);
	}

	public void form_mainMenu(PlayerFormRespondedEvent event, Object data) {
		String name = (String) data;
		String label = ((FormResponseSimple) event.getResponse()).getClickedButton().getText();

		if (label.equals("칭호 확인")) {
			FormWindowSimple window = new FormWindowSimple("칭호 확인", "");
			String text = "현재 사용중인 칭호: " + NameTagAPI.getInstance().getTag(name) + "\n\n";
			ArrayList<String> arr = NameTagAPI.getInstance().getTags(name);
			if (arr.size() > 0) {
				text += "보유중인 칭호 목록\n";
				for (String tag : arr)
					text += tag + "\n";
			} else
				text += "보유중인 칭호가 없습니다.";
			window.setContent(text);
			SimpleDialog.sendDialog(null, null, event.getPlayer(), window);
		}

		else if (label.equals("칭호 추가")) {
			FormWindowCustom window = new FormWindowCustom("칭호 추가");
			window.addElement(new ElementInput("추가할 칭호를 입력해주세요."));
			window.addElement(new ElementToggle("칭호를 바로 적용할까요?", true));
			SimpleDialog.sendDialog(this, "form_addTag", event.getPlayer(), window, name);
		}

		else if (label.equals("칭호 변경")) {
			FormWindowSimple window = new FormWindowSimple("칭호 변경", "사용하고 싶은 칭호를 선택해주세요.");
			ArrayList<String> arr = NameTagAPI.getInstance().getTags(name);
			if (arr.size() == 0)
				window.setContent("칭호를 보유하고있지 않습니다.");
			else
				for (String tag : arr)
					window.addButton(new ElementButton(tag));
			SimpleDialog.sendDialog(this, "form_setTag", event.getPlayer(), window, name);
		}

		else if (label.equals("칭호 변경 (OP)")) {
			FormWindowCustom window = new FormWindowCustom("칭호 강제변경");
			window.addElement(new ElementInput("변경할 칭호를 입력해주세요."));
			SimpleDialog.sendDialog(this, "form_setTagForce", event.getPlayer(), window, name);
		}

		else if (label.equals("칭호 삭제")) {
			FormWindowSimple window = new FormWindowSimple("칭호 삭제", "삭제하고 싶은 칭호를 선택해주세요.");
			ArrayList<String> arr = NameTagAPI.getInstance().getTags(name);
			if (arr.size() == 0)
				window.setContent("칭호를 보유하고있지 않습니다.");
			else
				for (String tag : arr)
					window.addButton(new ElementButton(tag));
			SimpleDialog.sendDialog(this, "form_removeTag", event.getPlayer(), window, name);
		}

		else if (label.equals("칭호 초기화")) {
			FormWindowModal window = new FormWindowModal("칭호 초기화", "정말 " + name + "님의 모든 칭호를 삭제하시겠습니까?\n초기화 후에는 복구할수 없습니다.", "예", "아니요");
			SimpleDialog.sendDialog(this, "form_resetTag", event.getPlayer(), window, name);
		}
	}

	public void form_addTag(PlayerFormRespondedEvent event, Object data) {
		FormResponseCustom response = (FormResponseCustom) event.getResponse();
		String tag = response.getInputResponse(0);
		if (tag.trim().length() != 0) {
			if (response.getToggleResponse(1))
				NameTagAPI.getInstance().setTag((String) data, tag);
			NameTagAPI.getInstance().addTag((String) data, tag);
			event.getPlayer().sendMessage("성공적으로 적용되었습니다.");
		} else {
			FormWindowSimple window = new FormWindowSimple("오류", "칭호는 공백이 될수 없습니다.");
			SimpleDialog.sendDialog(null, null, event.getPlayer(), window);
		}
	}

	public void form_setTag(PlayerFormRespondedEvent event, Object data) {
		FormResponseSimple response = (FormResponseSimple) event.getResponse();
		NameTagAPI.getInstance().setTag((String) data, response.getClickedButton().getText());
		event.getPlayer().sendMessage(data + "님의 칭호가 업데이트되었습니다.");
	}

	public void form_setTagForce(PlayerFormRespondedEvent event, Object data) {
		FormResponseCustom response = (FormResponseCustom) event.getResponse();
		String tag = response.getInputResponse(0);
		if (tag.trim().length() != 0) {
			NameTagAPI.getInstance().setTag((String) data, tag);
			event.getPlayer().sendMessage("성공적으로 적용되었습니다.");
		} else {
			FormWindowSimple window = new FormWindowSimple("오류", "칭호는 공백이 될수 없습니다.");
			SimpleDialog.sendDialog(null, null, event.getPlayer(), window);
		}
	}

	public void form_removeTag(PlayerFormRespondedEvent event, Object data) {
		FormResponseSimple response = (FormResponseSimple) event.getResponse();
		NameTagAPI.getInstance().removeTag((String) data, response.getClickedButton().getText());
		event.getPlayer().sendMessage(data + "님의 칭호가 업데이트되었습니다.");
	}

	public void form_resetTag(PlayerFormRespondedEvent event, Object data) {
		if (((FormResponseModal) event.getResponse()).getClickedButtonId() == 0) {
			NameTagAPI.getInstance().removeAllTag((String) data);
			event.getPlayer().sendMessage("성공하였습니다.");
		} else
			event.getPlayer().sendMessage("취소되었습니다..");
	}

	public boolean isInteger(String num) {
		try {
			if (Integer.parseInt(num) == Double.parseDouble(num))
				return true;
		} catch (Exception err) {
		}
		return false;
	}
}
