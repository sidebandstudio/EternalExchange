package moze_intel.projecte.gameObjs.blocks;

import com.mojang.serialization.MapCodec;
import moze_intel.projecte.compat.network.PEMenuHelper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.block_entities.IItemHandlerBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import moze_intel.projecte.compat.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MatterFurnace extends AbstractFurnaceBlock implements IMatterBlock, PEEntityBlock<DMFurnaceBlockEntity> {

	private final EnumMatterType matterType;

	public MatterFurnace(Properties props, EnumMatterType type) {
		super(props);
		this.matterType = type;
	}

	@Nullable
	@Override
	public BlockEntityTypeRegistryObject<? extends DMFurnaceBlockEntity> getType() {
		return matterType == EnumMatterType.RED_MATTER ? PEBlockEntityTypes.RED_MATTER_FURNACE : PEBlockEntityTypes.DARK_MATTER_FURNACE;
	}

	@NotNull
	@Override
	protected MapCodec<MatterFurnace> codec() {
		//Fabric port: get() keeps the specialized codec type (PEDeferredHolder#value() returns the registry base type)
		return PEBlockTypes.MATTER_FURNACE.get();
	}

	@Override
	protected void openContainer(Level level, @NotNull BlockPos pos, @NotNull Player player) {
		if (!level.isClientSide) {
			DMFurnaceBlockEntity furnace = WorldHelper.getBlockEntity(DMFurnaceBlockEntity.class, level, pos, true);
			if (furnace != null && player instanceof ServerPlayer serverPlayer) {
				//Fabric port: replaces NeoForge's openMenu(provider, pos); the client container factory reads the position back
				PEMenuHelper.openMenu(serverPlayer, furnace, buf -> buf.writeBlockPos(pos));
			}
		}
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			//Fabric port: no block item handler capability lookup; drop the inventory our own block entity exposes
			IItemHandler handler = WorldHelper.getBlockEntity(level, pos) instanceof IItemHandlerBlockEntity handlerBE ? handlerBE.getItemHandler(null) : null;
			WorldHelper.dropInventory(handler, level, pos);
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	@Deprecated
	public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
		//Fabric port: no block item handler capability lookup; read the inventory our own block entity exposes
		return IItemHandlerBlockEntity.calcRedstoneFromInventory(
				WorldHelper.getBlockEntity(level, pos) instanceof IItemHandlerBlockEntity handlerBE ? handlerBE.getItemHandler(null) : null);
	}

	@Override
	public EnumMatterType getMatterType() {
		return matterType;
	}
}