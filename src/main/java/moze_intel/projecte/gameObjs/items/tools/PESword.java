package moze_intel.projecte.gameObjs.items.tools;

import java.util.List;
import java.util.function.Consumer;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.items.IHasConditionalAttributes;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import moze_intel.projecte.compat.events.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

public class PESword extends SwordItem implements IExtraFunction, IItemCharge, IBarHelper, IHasConditionalAttributes {

	private final IMatterType matterType;
	private final int numCharges;

	//Fabric port: vanilla SwordItem has no (Tier, Properties, Tool) constructor and bakes the tier's TOOL component;
	// overriding components() swaps in the custom tool rules instead
	private final net.minecraft.core.component.DataComponentMap componentsWithTool;

	public PESword(IMatterType matterType, int numCharges, int damage, Properties props) {
		super(matterType, props.attributes(createAttributes(matterType, damage, -2.4F))
				.component(PEDataComponentTypes.CHARGE.get(), 0)
				.component(PEDataComponentTypes.STORED_EMC.get(), 0L));
		this.componentsWithTool = net.minecraft.core.component.DataComponentMap.composite(super.components(),
				net.minecraft.core.component.DataComponentMap.builder().set(net.minecraft.core.component.DataComponents.TOOL, new Tool(List.of(
						Tool.Rule.deniesDrops(matterType.getIncorrectBlocksForDrops()),
						Tool.Rule.minesAndDrops(PETags.Blocks.MINEABLE_WITH_PE_SWORD, matterType.getSpeed()),
						Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)
				), 1.0F, 2)).build());
		this.matterType = matterType;
		this.numCharges = numCharges;
	}

	@Override
	@NotNull
	public net.minecraft.core.component.DataComponentMap components() {
		//Fabric port: see constructor note
		return componentsWithTool == null ? super.components() : componentsWithTool;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canBeEnchantedWith(@NotNull ItemStack stack, @NotNull net.minecraft.core.Holder<Enchantment> enchantment, @NotNull net.fabricmc.fabric.api.item.v1.EnchantingContext context) {
		//Fabric port: collapses NeoForge's isBookEnchantable/isPrimaryItemFor/supportsEnchantment
		return false;
	}


	@Override
	public boolean isBarVisible(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public float getWidthForBar(ItemStack stack) {
		return 1 - getChargePercent(stack);
	}

	@Override
	public int getBarWidth(@NotNull ItemStack stack) {
		return getScaledBarWidth(stack);
	}

	@Override
	public int getBarColor(@NotNull ItemStack stack) {
		return getColorForBar(stack);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		return ToolHelper.getDestroySpeed(super.getDestroySpeed(stack, state), matterType, getCharge(stack));
	}

	@Override
	public int getNumCharges(@NotNull ItemStack stack) {
		return numCharges;
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
		ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
		return true;
	}

	/**
	 * Fabric port: NeoForge sweep-box hook; needs a Player#attack mixin to take effect.
	 */
	@NotNull
	public AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
		int charge = getCharge(stack);
		return target.getBoundingBox().inflate(charge, charge / 4D, charge);
	}

	@Override
	public boolean doExtraFunction(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
		if (player.getAttackStrengthScale(0F) == 1) {
			ToolHelper.attackAOE(stack, player, slayAll(stack), (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE), 0, hand);
			PlayerHelper.resetCooldown(player);
			return true;
		}
		return false;
	}

	protected boolean slayAll(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public void adjustAttributes(ItemAttributeModifierEvent event) {
		ToolHelper.applyChargeAttributes(event);
	}
}