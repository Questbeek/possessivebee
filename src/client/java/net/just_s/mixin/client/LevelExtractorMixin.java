package net.just_s.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @Shadow protected abstract EntityRenderState extractEntity(Entity entity, float partialTick);

    @Inject(method = "extractVisibleEntities", at = @At("TAIL"))
    private void possessive$renderPlayerOriginalBodyWhenPossessing(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            LocalPlayer entity = Minecraft.getInstance().player;
            if (entity == null) {
                return;
            }

            TickRateManager tickRateManager = Minecraft.getInstance().level.tickRateManager();
            float g = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
            EntityRenderState entityRenderState = extractEntity(entity, g);
            levelRenderState.entityRenderStates.add(entityRenderState);
            if (entityRenderState.appearsGlowing() && entity.isCurrentlyGlowing()) {
                levelRenderState.shouldShowEntityOutlines = true;
            }
        }
    }

    @WrapOperation(
            method = "extractVisibleEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;isEntityVisible(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDFJ)Z"
            )
    )
    private boolean possessive$onRenderEntity(LevelExtractor instance, Entity entity, Frustum frustum, double d, double e, double f, float partialTicks, long chunkFadeDuration, Operation<Boolean> original) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return PossessiveModClient.cameraHandler.getCamera().shouldRenderEntity(entity);
        }
        return original.call(instance, entity, frustum, d, e, f, partialTicks, chunkFadeDuration);
    }
}
