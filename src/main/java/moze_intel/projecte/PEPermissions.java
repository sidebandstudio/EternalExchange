package moze_intel.projecte;

import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

//Fabric port: NeoForge's PermissionAPI does not exist on Fabric, so the permission nodes are replaced with plain vanilla
// permission level checks matching the fallback levels the upstream nodes defaulted to (level 0 for the root /projecte
// command, op level for the sub commands). Constant names are kept so command classes stay diffable against upstream.
public class PEPermissions {

	//Commands
	public static final CommandPermissionNode COMMAND = new CommandPermissionNode(Commands.LEVEL_ALL);

	public static final CommandPermissionNode COMMAND_REMOVE_EMC = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_RESET_EMC = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_SET_EMC = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_SHOW_BAG = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_EMC = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_EMC_ADD = nodeSubCommand(COMMAND_EMC);
	public static final CommandPermissionNode COMMAND_EMC_REMOVE = nodeSubCommand(COMMAND_EMC);
	public static final CommandPermissionNode COMMAND_EMC_SET = nodeSubCommand(COMMAND_EMC);
	public static final CommandPermissionNode COMMAND_EMC_TEST = nodeSubCommand(COMMAND_EMC);
	public static final CommandPermissionNode COMMAND_EMC_GET = nodeSubCommand(COMMAND_EMC);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE = nodeOpCommand();
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_CLEAR = nodeSubCommand(COMMAND_KNOWLEDGE);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_LEARN = nodeSubCommand(COMMAND_KNOWLEDGE);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_UNLEARN = nodeSubCommand(COMMAND_KNOWLEDGE);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_TEST = nodeSubCommand(COMMAND_KNOWLEDGE);

	private static CommandPermissionNode nodeOpCommand() {
		return new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	}

	private static CommandPermissionNode nodeSubCommand(CommandPermissionNode parent) {
		//Sub commands can assume that the parent was checked before getting to them, so they just inherit the parent's level
		return new CommandPermissionNode(parent.fallbackLevel());
	}

	public record CommandPermissionNode(int fallbackLevel) implements Predicate<CommandSourceStack> {

		@Override
		public boolean test(CommandSourceStack source) {
			return source.hasPermission(fallbackLevel);
		}
	}
}
