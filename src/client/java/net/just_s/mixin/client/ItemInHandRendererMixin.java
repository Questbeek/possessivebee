package net.just_s.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {

    @Unique
    private float possessive$tickDelta;

    @Inject(
            method = "submitHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(FFF)F",
                    ordinal = 0
            )
    )
    private void possessive$replaceBooleansForRenderingHands(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int i, CallbackInfo ci, @Local LocalRef<FirstPersonHandsAndItemsRenderer.HandRenderSelection> localRef){
        // if mod not in use then just skip it altogether
        if (!PossessiveModClient.cameraHandler.isEnabled()) {
            return;
        }

        boolean finalShouldRenderMainHand = PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInMainHand(localRef.get().renderMainHand);
        boolean finalShouldRenderOffHand = PossessiveModClient.cameraHandler.getCamera().shouldRenderItemInOffHand(localRef.get().renderOffHand);

        if (finalShouldRenderMainHand && finalShouldRenderOffHand) {
            localRef.set(FirstPersonHandsAndItemsRenderer.HandRenderSelection.RENDER_BOTH_HANDS);
        } else if (finalShouldRenderMainHand) {
            localRef.set(FirstPersonHandsAndItemsRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY);
        } else if (finalShouldRenderOffHand) {
            localRef.set(FirstPersonHandsAndItemsRenderer.HandRenderSelection.RENDER_OFF_HAND_ONLY);
        }
    }

    @ModifyArgs(
            method = "submitHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    // signature omitted on purpose: the 26.2 descriptor is not verified, Mixin matches by name
					target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitArmWithItem"
            )
    )
    private void possessive$replaceItemToRender(Args args){
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            // 3rd arg is InteractionHand, 5th arg is ItemStack
            args.set(5, PossessiveModClient.cameraHandler.getCamera().getItemToRender(args.get(3)));
            // 9th arg is light level
            args.set(9, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(PossessiveModClient.cameraHandler.getCamera(), possessive$tickDelta));
        }
    }
}
