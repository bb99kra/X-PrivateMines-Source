package dev.drawethree.xprivatemines.command;

import dev.drawethree.xprivatemines.XPrivateMines;
import dev.drawethree.xprivatemines.command.sub.PrivateMineSubCommand;
import dev.drawethree.xprivatemines.command.sub.impl.AddonsCmd;
import dev.drawethree.xprivatemines.command.sub.impl.AdminListCmd;
import dev.drawethree.xprivatemines.command.sub.impl.BanCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ClaimCmd;
import dev.drawethree.xprivatemines.command.sub.impl.CloseCmd;
import dev.drawethree.xprivatemines.command.sub.impl.CreateCmd;
import dev.drawethree.xprivatemines.command.sub.impl.EntryFeeCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ExpandCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ForceCreateCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ForceDeleteCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ForceExpandCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ForceSetBlockCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ForceUpgradeCmd;
import dev.drawethree.xprivatemines.command.sub.impl.HelpCmd;
import dev.drawethree.xprivatemines.command.sub.impl.KickCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ListCmd;
import dev.drawethree.xprivatemines.command.sub.impl.MigrateCmd;
import dev.drawethree.xprivatemines.command.sub.impl.OpenCmd;
import dev.drawethree.xprivatemines.command.sub.impl.PregenCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ReloadCmd;
import dev.drawethree.xprivatemines.command.sub.impl.ResetCmd;
import dev.drawethree.xprivatemines.command.sub.impl.SchematicSetupCmd;
import dev.drawethree.xprivatemines.command.sub.impl.SetMotdCmd;
import dev.drawethree.xprivatemines.command.sub.impl.SetNameCmd;
import dev.drawethree.xprivatemines.command.sub.impl.StatusCmd;
import dev.drawethree.xprivatemines.command.sub.impl.StopPregenCmd;
import dev.drawethree.xprivatemines.command.sub.impl.TaxCmd;
import dev.drawethree.xprivatemines.command.sub.impl.TeleportCmd;
import dev.drawethree.xprivatemines.command.sub.impl.TopCmd;
import dev.drawethree.xprivatemines.command.sub.impl.UnbanCmd;
import dev.drawethree.xprivatemines.command.sub.impl.UpgradeCmd;
import dev.drawethree.xprivatemines.gui.PrivateMineGUI;
import dev.drawethree.xprivatemines.mines.model.PrivateMineImpl;
import dev.drawethree.xprivatemines.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Generated;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PrivateMineCommand {
   public static final String ADMIN_PERM = "xprivatemines.admin";
   private final XPrivateMines plugin;
   private final Map<String, PrivateMineSubCommand> subCommands;

   public PrivateMineCommand(XPrivateMines plugin) {
      this.plugin = plugin;
      this.subCommands = new LinkedHashMap<>();
   }

   private void registerSubCommands() {
      this.registerSubCommand(new PregenCmd(this));
      this.registerSubCommand(new StopPregenCmd(this));
      this.registerSubCommand(new StatusCmd(this));
      this.registerSubCommand(new ForceCreateCmd(this));
      this.registerSubCommand(new ForceDeleteCmd(this));
      this.registerSubCommand(new ForceExpandCmd(this));
      this.registerSubCommand(new ForceUpgradeCmd(this));
      this.registerSubCommand(new ForceSetBlockCmd(this));
      this.registerSubCommand(new CreateCmd(this));
      this.registerSubCommand(new TeleportCmd(this));
      this.registerSubCommand(new UpgradeCmd(this));
      this.registerSubCommand(new ExpandCmd(this));
      this.registerSubCommand(new ResetCmd(this));
      this.registerSubCommand(new OpenCmd(this));
      this.registerSubCommand(new CloseCmd(this));
      this.registerSubCommand(new EntryFeeCmd(this));
      this.registerSubCommand(new TaxCmd(this));
      this.registerSubCommand(new ClaimCmd(this));
      this.registerSubCommand(new KickCmd(this));
      this.registerSubCommand(new BanCmd(this));
      this.registerSubCommand(new UnbanCmd(this));
      this.registerSubCommand(new AdminListCmd(this));
      this.registerSubCommand(new ListCmd(this));
      this.registerSubCommand(new HelpCmd(this));
      this.registerSubCommand(new MigrateCmd(this));
      this.registerSubCommand(new ReloadCmd(this));
      this.registerSubCommand(new AddonsCmd(this));
      this.registerSubCommand(new SetNameCmd(this));
      this.registerSubCommand(new SetMotdCmd(this));
      this.registerSubCommand(new TopCmd(this));
      this.registerSubCommand(new SchematicSetupCmd(this));
   }

   public void register() {
      this.registerSubCommands();
      this.registerMainCommand();
   }

   private void registerMainCommand() {
      Commands.create().tabHandler(this::createTabHandler).handler(c -> {
         if (c.args().isEmpty()) {
            if (c.sender() instanceof Player) {
               PrivateMineImpl mine = this.plugin.getMinesManager().getPrivateMineInternal((OfflinePlayer)c.sender());
               if (mine == null) {
                  PlayerUtils.sendMessage(c.sender(), this.plugin.getMessageConfig().getMessage("no-mine"));
                  return;
               }

               this.openPMinePanelGui(mine, (Player)c.sender());
               return;
            }

            if (c.sender() instanceof ConsoleCommandSender) {
               this.getHelpSubCommand().execute(c.sender(), c.args());
               return;
            }
         }

         PrivateMineSubCommand subCommand = this.getSubCommand(Objects.requireNonNull(c.rawArg(0)));
         if (subCommand != null) {
            if (!subCommand.canExecute(c.sender())) {
               PlayerUtils.sendMessage(c.sender(), this.plugin.getMessageConfig().getMessage("no-perm"));
               return;
            }

            if (!subCommand.execute(c.sender(), c.args().subList(1, c.args().size()))) {
               PlayerUtils.sendMessage(c.sender(), subCommand.getUsage());
            }
         } else {
            this.getHelpSubCommand().execute(c.sender(), c.args());
         }
      }).registerAndBind(this.plugin, this.plugin.getPrivateMinesConfig().getMainCommandAliases());
   }

   private PrivateMineSubCommand getHelpSubCommand() {
      return this.getSubCommand("help");
   }

   private List<String> createTabHandler(CommandContext<CommandSender> context) {
      List<String> tab = new ArrayList<>();

      for (PrivateMineSubCommand subCommand : this.subCommands.values()) {
         if (subCommand.canExecute(context.sender())) {
            tab.add(subCommand.getName());
         }
      }

      if (context.args().isEmpty()) {
         return tab;
      } else {
         PrivateMineSubCommand subCommandx = this.getSubCommand(context.rawArg(0));
         return subCommandx != null && subCommandx.canExecute(context.sender())
            ? subCommandx.getTabComplete(context.args().subList(1, context.args().size()))
            : tab;
      }
   }

   public Collection<PrivateMineSubCommand> getSubCommands() {
      return this.subCommands.values();
   }

   private void registerSubCommand(PrivateMineSubCommand command) {
      this.subCommands.put(command.getName(), command);

      for (String alias : command.getAliases()) {
         this.subCommands.put(alias, command);
      }
   }

   private PrivateMineSubCommand getSubCommand(String arg) {
      return this.subCommands.get(arg.toLowerCase());
   }

   private void openPMinePanelGui(PrivateMineImpl mine, Player player) {
      new PrivateMineGUI(player, mine).open();
   }

   @Generated
   public XPrivateMines getPlugin() {
      return this.plugin;
   }
}
