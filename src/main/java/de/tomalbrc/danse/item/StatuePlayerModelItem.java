package de.tomalbrc.danse.item;

import com.mojang.authlib.GameProfile;
import de.tomalbrc.danse.entity.StatuePlayerModelEntity;
import de.tomalbrc.danse.registry.EntityRegistry;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class StatuePlayerModelItem extends ArmorStandItem implements PolymerItem {

    public StatuePlayerModelItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext useOnContext) {
        Direction direction = useOnContext.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level level = useOnContext.getLevel();
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
            BlockPos blockPos = blockPlaceContext.getClickedPos();
            ItemStack itemStack = useOnContext.getItemInHand();
            Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
            AABB aABB = EntityRegistry.PLAYER_STATUE.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
            if (level.noCollision(null, aABB) && level.getEntities(null, aABB).isEmpty()) {
                if (level instanceof ServerLevel serverLevel) {
                    Consumer<StatuePlayerModelEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, useOnContext.getPlayer());
                    StatuePlayerModelEntity statue = EntityRegistry.PLAYER_STATUE.create(serverLevel, consumer, blockPos, EntitySpawnReason.SPAWN_ITEM_USE, true, true);
                    if (statue == null) {
                        return InteractionResult.FAIL;
                    }

                    statue.setAnyModel();

                    var profile = itemStack.get(DataComponents.PROFILE);
                    if (profile != null) {
                        profile.resolve().thenAccept(resolvableProfile -> statue.setProfile(Optional.of(resolvableProfile.gameProfile())));
                    }
                    else {
                        statue.setProfile(Optional.of(new GameProfile(UUID.fromString("0"), "")));
                    }

                    float yRot = (float) Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    statue.snapTo(statue.getX(), statue.getY(), statue.getZ(), yRot, 0);
                    serverLevel.addFreshEntityWithPassengers(statue);
                    level.playSound(null, statue.getX(), statue.getY(), statue.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    statue.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());
                }

                itemStack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.ARMOR_STAND;
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
        return (resolvableProfile != null && resolvableProfile.name().isPresent() ? Component.translatable(this.descriptionId + ".named", resolvableProfile.name().get()) : super.getName(itemStack));
    }
}
