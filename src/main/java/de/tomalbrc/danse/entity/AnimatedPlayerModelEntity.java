package de.tomalbrc.danse.entity;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.danse.poly.PlayerPartHolder;
import de.tomalbrc.danse.registry.PlayerModelRegistry;
import de.tomalbrc.danse.util.Util;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

// persistent model
public class AnimatedPlayerModelEntity extends StatuePlayerModelEntity implements AnimatedEntity {
    public static final ResourceLocation ID = Util.id("player_model");
    protected static final String ANIMATION = "Animation";

    @Nullable
    protected String animation = null;

    public AnimatedPlayerModelEntity(EntityType<? extends StatuePlayerModelEntity> entityType, Level level) {
        super(entityType, level);

        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setModel(Model model) {
        if (this.holder != null)
            holder.destroy();

        this.holder = new PlayerPartHolder<>(this, model);
        this.holder.setupHitbox();
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueInput) {

        valueInput.getString(ANIMATION).ifPresent(anim -> {
            if (this.holder != null) {
                this.holder.destroy();
            }

            if (this.animation != null && !this.animation.isBlank() && this.holder != null) {
                this.holder.getAnimator().stopAnimation(this.animation);
            }

            this.animation = anim;

            if (!this.animation.isBlank()) {
                var model = PlayerModelRegistry.getModel(this.animation);
                if (model != null && model.animations().containsKey(this.animation)) {
                    this.setModel(model);
                    this.holder.getAnimator().playAnimation(this.animation, () -> this.animation = null);
                }
            }
        });

        super.readAdditionalSaveData(valueInput);

        var yRot = valueInput.getFloatOr("Yaw", Float.NaN);
        if (!Float.isNaN(yRot)) {
            this.setYRot(yRot);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);

        if (this.animation != null && !this.animation.isBlank()) {
            valueOutput.putString(ANIMATION, this.animation);
        }

        valueOutput.putFloat("Yaw", this.getYRot());
    }

    public void playAnimation(String animation, Runnable onFinish) {
        this.animation = animation;
        this.holder.getAnimator().playAnimation(animation, onFinish);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (damageSource.isCreativePlayer())
            this.kill(serverLevel);
        return damageSource.isCreativePlayer();
    }
}

