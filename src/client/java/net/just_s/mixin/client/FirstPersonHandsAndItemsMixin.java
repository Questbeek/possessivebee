package net.just_s.mixin.client;

import net.just_s.PossessiveModClient;
import net.just_s.camera.AbstractCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 26.3 which hands are drawn and which items they hold is decided while the render state is extracted,
 * the renderer itself only draws that state. So the possessed entity's items are put into the state here.
 */
@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void possessive$replaceHandsAndItems(LocalPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        // if mod not in use then just skip it altogether
        if (!PossessiveModClient.cameraHandler.isEnabled()) {
            return;
        }
        AbstractCamera camera = PossessiveModClient.cameraHandler.getCamera();
        Minecraft minecraft = Minecraft.getInstance();

        // Which hands are rendered
        boolean renderMainHand = camera.shouldRenderItemInMainHand(state.handRenderSelection.renderMainHand);
        boolean renderOffHand = camera.shouldRenderItemInOffHand(state.handRenderSelection.renderOffHand);
        if (renderMainHand && renderOffHand) {
            state.handRenderSelection = FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        } else if (renderMainHand) {
            state.handRenderSelection = FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_MAIN_HAND_ONLY;
        } else if (renderOffHand) {
            state.handRenderSelection = FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_OFF_HAND_ONLY;
        }

        // Which items they hold
        boolean isMainHandRight = player.getMainArm() == HumanoidArm.RIGHT;
        ItemDisplayContext mainHandContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemDisplayContext offHandContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        state.mainHandItem = camera.getItemToRender(InteractionHand.MAIN_HAND);
        state.offHandItem = camera.getItemToRender(InteractionHand.OFF_HAND);

        state.mainHandRenderState.clear();
        state.offHandRenderState.clear();
        minecraft.getItemModelResolver().updateForTopItem(
                state.mainHandRenderState, state.mainHandItem, mainHandContext, player.level(), player, player.getId() + mainHandContext.ordinal()
        );
        minecraft.getItemModelResolver().updateForTopItem(
                state.offHandRenderState, state.offHandItem, offHandContext, player.level(), player, player.getId() + offHandContext.ordinal()
        );

        state.mainHandUseDuration = state.mainHandItem.getUseDuration(player);
        state.offHandUseDuration = state.offHandItem.getUseDuration(player);
        state.mainHandChargeDuration = CrossbowItem.getChargeDuration(state.mainHandItem, player);
        state.offHandChargeDuration = CrossbowItem.getChargeDuration(state.offHandItem, player);
        state.mainHandSwapScale = minecraft.getItemModelResolver().swapAnimationScale(state.mainHandItem);
        state.offHandSwapScale = minecraft.getItemModelResolver().swapAnimationScale(state.offHandItem);
        // the possessed entity's items are never rendered as maps
        state.hasMainHandMapData = false;
        state.hasOffHandMapData = false;
    }
}
