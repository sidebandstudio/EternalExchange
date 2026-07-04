package moze_intel.projecte.compat.network;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: replacement for NeoForge's ServerPlayer#openMenu(MenuProvider, Consumer&lt;RegistryFriendlyByteBuf&gt;) extension. The buffer is delivered
 * to the client factory through the raw-buffer ExtendedScreenHandlerType set up in ContainerTypeDeferredRegister.
 */
public final class PEMenuHelper {

	private PEMenuHelper() {
	}

	public static void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> bufWriter) {
		player.openMenu(new ExtendedScreenHandlerFactory<RegistryFriendlyByteBuf>() {
			@Override
			public RegistryFriendlyByteBuf getScreenOpeningData(ServerPlayer serverPlayer) {
				RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
				bufWriter.accept(buf);
				return buf;
			}

			@Override
			public Component getDisplayName() {
				return provider.getDisplayName();
			}

			@Nullable
			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
				return provider.createMenu(containerId, inventory, p);
			}
		});
	}
}
