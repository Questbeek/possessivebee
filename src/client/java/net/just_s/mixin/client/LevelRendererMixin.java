package net.just_s.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.just_s.PossessiveModClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private LevelTargetBundle targets;
    @Shadow @Final private GameRenderer gameRenderer;

    // 26.3 has no transparency post chain to hook into any more, so the camera's shader is added
    // to the frame graph right before the graph is executed (the only execute call in render).
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute"
            )
    )
    private void possessive$onRenderLevel(CallbackInfo ci, @Local FrameGraphBuilder frame) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            PossessiveModClient.cameraHandler.getCamera().onCameraShader(
                    frame,
                    this.gameRenderer.mainRenderTarget().width,
                    this.gameRenderer.mainRenderTarget().height,
                    this.targets
            );
        }
    }
}
