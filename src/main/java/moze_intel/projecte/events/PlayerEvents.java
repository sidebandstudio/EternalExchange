package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.compat.items.IItemHandler;
import moze_intel.projecte.compat.items.ItemHandlerHelper;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.gameObjs.items.armor.PEArmor.ReductionInfo;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port: upstream's event-bus subscribers, rewritten onto Fabric events where they exist and mixin entry points where they do not (item pickup, fire
 * invulnerability, incoming damage reduction — see moze_intel.projecte.mixin).
 */
public class PlayerEvents {

	public static void register() {
		//On death or return from end, sync to the client
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> {
			syncData(player);
			//Fabric port: replaces the EntityConstructing offline-cache clear for respawning players
			TransmutationOffline.clear(player.getUUID());
		});
		//Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> syncData(player));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			TransmutationOffline.clear(player.getUUID());
			IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
			if (knowledge != null) {
				knowledge.sync(player);
				PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
			}
			IAlchBagProvider alchBagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
			if (alchBagProvider != null) {
				alchBagProvider.syncAllBags(player);
			}
			PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
			onHighAlchemistJoin(player);
		});
	}

	private static void syncData(ServerPlayer player) {
		IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
		if (knowledge != null) {
			knowledge.sync(player);
		}
		IAlchBagProvider bagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
		if (bagProvider != null) {
			bagProvider.syncAllBags(player);
		}
	}

	private static void onHighAlchemistJoin(ServerPlayer player) {
		if (PECore.uuids.contains(player.getUUID().toString())) {
			MinecraftServer server = player.getServer();
			if (server != null) {
				Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, player.getDisplayName());
				server.getPlayerList().broadcastSystemMessage(joinMessage, false);
			}
		}
	}

	/**
	 * Alchemical bag suction. Called from ItemEntityPickupMixin at the head of ItemEntity#playerTouch.
	 *
	 * @return true if vanilla pickup should be cancelled
	 */
	public static boolean onItemPickup(ItemEntity itemEntity, Player player) {
		if (itemEntity.level().isClientSide || itemEntity.hasPickUpDelay() || itemEntity.getTarget() != null && !player.getUUID().equals(itemEntity.getTarget())) {
			return false;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (bag.isEmpty()) {
			return false;
		}
		IAlchBagProvider bagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
		if (bagProvider == null) {
			return false;
		}
		ItemStack stack = itemEntity.getItem();
		IItemHandler handler = bagProvider.getBag(((AlchemicalBag) bag.getItem()).color);
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack, false);
		int pickedUpCount = stack.getCount() - remainder.getCount();
		if (pickedUpCount > 0) {
			player.take(itemEntity, pickedUpCount);
			if (remainder.isEmpty()) {
				itemEntity.discard();
				//Update to the picked up count so that onItemPickup knows how much got picked up
				stack.setCount(pickedUpCount);
			} else {
				//Fabric port: shrink the item entity ourselves since there is no pickup event doing the bookkeeping
				itemEntity.setItem(remainder);
			}
			player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), pickedUpCount);
			player.onItemPickup(itemEntity);
			return true;
		}
		return false;
	}

	/**
	 * Fire invulnerability from PE armor/items. Called from PlayerInvulnerabilityMixin.
	 */
	public static boolean isInvulnerableTo(Player player, DamageSource source) {
		return player instanceof ServerPlayer serverPlayer && source.is(DamageTypeTags.IS_FIRE) && TickEvents.shouldPlayerResistFire(serverPlayer);
	}

	/**
	 * PE armor percent-based damage reduction. Called from PlayerDamageMixin before vanilla armor calculations; returns the adjusted damage.
	 */
	public static float modifyIncomingDamage(Player player, DamageSource source, float amount) {
		if (amount <= 0) {
			return amount;
		}
		ReductionInfo reductionInfo = ReductionInfo.ZERO;
		for (ItemStack armorStack : player.getArmorSlots()) {
			if (armorStack.getItem() instanceof PEArmor armorItem) {
				//We return the max of this piece's base reduction (in relation to the full set),
				// and the max damage an item can absorb for a given source
				reductionInfo = reductionInfo.add(armorItem.getReductionInfo(source));
			}
		}
		if (reductionInfo.percentReduced() >= 1) {
			return 0;
		} else if (reductionInfo.maxDamagedAbsorbed() > 0 && reductionInfo.percentReduced() > 0) {
			float damageAbsorbed = Math.min(amount * reductionInfo.percentReduced(), reductionInfo.maxDamagedAbsorbed());
			return Math.max(0, amount - damageAbsorbed);
		}
		return amount;
	}
}
