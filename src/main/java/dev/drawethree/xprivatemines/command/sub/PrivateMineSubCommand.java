package dev.drawethree.xprivatemines.command.sub;

import dev.drawethree.xprivatemines.command.PrivateMineCommand;
import dev.drawethree.xprivatemines.config.MessageConfig;
import dev.drawethree.xprivatemines.config.PrivateMinesConfig;
import dev.drawethree.xprivatemines.manager.MineTierManagerImpl;
import dev.drawethree.xprivatemines.manager.PrivateMinesManagerImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.command.CommandSender;

public abstract class PrivateMineSubCommand {
   public static final String COMMAND_PERMISSION_ROOT = "xprivatemines.command.";
   protected final Map<String, PrivateMineSubCommand> subCommands;
   protected final PrivateMineCommand command;
   protected final String name;
   protected final String[] aliases;

   public PrivateMineSubCommand(PrivateMineCommand command, String name, String... aliases) {
      this.command = command;
      this.name = name;
      this.aliases = aliases;
      this.subCommands = new HashMap<>();
   }

   public abstract boolean execute(CommandSender var1, List<String> var2);

   public abstract String getUsage();

   public boolean canExecute(CommandSender sender) {
      return sender.isOp() || sender.hasPermission("xprivatemines.admin") || sender.hasPermission("xprivatemines.command." + this.getName());
   }

   public abstract List<String> getTabComplete(List<String> var1);

   protected void registerSubCommand(PrivateMineSubCommand subCommand) {
      this.subCommands.put(subCommand.getName(), subCommand);
   }

   protected PrivateMineSubCommand getSubCommand(String name) {
      return this.subCommands.get(name.toLowerCase());
   }

   protected PrivateMinesManagerImpl getMinesManager() {
      return this.getCommand().getPlugin().getMinesManager();
   }

   protected MineTierManagerImpl getTierManager() {
      return this.getCommand().getPlugin().getMineTierManager();
   }

   protected PrivateMinesConfig getConfig() {
      return this.getCommand().getPlugin().getPrivateMinesConfig();
   }

   protected MessageConfig getMessageConfig() {
      return this.getCommand().getPlugin().getMessageConfig();
   }

   @Generated
   public Map<String, PrivateMineSubCommand> getSubCommands() {
      return this.subCommands;
   }

   @Generated
   public PrivateMineCommand getCommand() {
      return this.command;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String[] getAliases() {
      return this.aliases;
   }

   @Generated
   public PrivateMineSubCommand(Map<String, PrivateMineSubCommand> subCommands, PrivateMineCommand command, String name, String[] aliases) {
      this.subCommands = subCommands;
      this.command = command;
      this.name = name;
      this.aliases = aliases;
   }
}
