package moze_intel.projecte.gameObjs.registration.impl;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.INamedEntry;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import moze_intel.projecte.utils.WorldHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerTypeDeferredRegister extends PEDeferredRegister<MenuType<?>> {

	/**
	 * Fabric port: passthrough codec so menus keep reading their opening data from a raw buffer like they did with NeoForge's IContainerFactory.
	 */
	public static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> RAW_BUFFER = StreamCodec.of(
			(out, data) -> out.writeBytes(data, data.readerIndex(), data.readableBytes()),
			buf -> {
				RegistryFriendlyByteBuf copy = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess());
				copy.writeBytes(buf, buf.readableBytes());
				return copy;
			});

	public ContainerTypeDeferredRegister(String modid) {
		super(Registries.MENU, modid, ContainerTypeRegistryObject::new);
	}

	public <CONTAINER extends AbstractContainerMenu, BE extends BlockEntity> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider,
			Class<BE> blockEntityClass, IBlockEntityContainerFactory<CONTAINER, BE> factory) {
		return register(nameProvider, (id, inv, buf) -> factory.create(id, inv, getBlockEntityFromBuf(buf, blockEntityClass)));
	}

	public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider, IContainerFactory<CONTAINER> factory) {
		return register(nameProvider.getName(), factory);
	}

	public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, IContainerFactory<CONTAINER> factory) {
		return registerMenu(name, () -> new ExtendedScreenHandlerType<>(factory::create, RAW_BUFFER));
	}

	@SuppressWarnings("unchecked")
	public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> registerMenu(String name, Supplier<MenuType<CONTAINER>> supplier) {
		return (ContainerTypeRegistryObject<CONTAINER>) super.register(name, supplier);
	}

	private static <BE extends BlockEntity> BE getBlockEntityFromBuf(FriendlyByteBuf buf, Class<BE> type) {
		if (buf == null) {
			throw new IllegalArgumentException("Null packet buffer");
		} else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			throw new UnsupportedOperationException("This method is only supported on the client.");
		}
		BlockPos pos = buf.readBlockPos();
		//Fabric port: resolved through ClientPayloadHelper so this class stays loadable on dedicated servers
		BE blockEntity = WorldHelper.getBlockEntity(type, moze_intel.projecte.network.packets.to_client.ClientPayloadHelper.getClientLevel(), pos);
		if (blockEntity == null) {
			throw new IllegalStateException("Client could not locate block entity at " + pos + " for block entity container. "
											+ "This is likely caused by a mod breaking client side block entity lookup");
		}
		return blockEntity;
	}

	/**
	 * Fabric port: mirrors NeoForge's IContainerFactory, receiving the opening data as a raw buffer.
	 */
	@FunctionalInterface
	public interface IContainerFactory<CONTAINER extends AbstractContainerMenu> {

		CONTAINER create(int id, Inventory inv, RegistryFriendlyByteBuf buf);
	}

	@FunctionalInterface
	public interface IBlockEntityContainerFactory<CONTAINER extends AbstractContainerMenu, BE extends BlockEntity> {

		CONTAINER create(int id, Inventory inv, BE blockEntity);
	}
}
