package blusunrize.immersiveengineering.client.models;

import com.google.common.base.Optional;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.TRSRTransformation;

@SuppressWarnings("deprecation")
public interface IOBJModelCallback
{
	public TextureAtlasSprite getTextureReplacement(ItemStack stack, String material);
	public boolean shouldRenderGroup(ItemStack stack, String group);
	public Optional<TRSRTransformation> applyTransformations(ItemStack stack, String group, Optional<TRSRTransformation> transform);
	public Matrix4 handlePerspective(ItemStack stack, TransformType cameraTransformType, Matrix4 perspective);
}