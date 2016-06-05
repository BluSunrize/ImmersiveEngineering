package blusunrize.immersiveengineering.client.models;

import com.google.common.base.Optional;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
//T must be ItemStack for Items or IBlockState for TileEntities implementing this
public interface IOBJModelCallback<T>
{
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTextureReplacement(T object, String material);
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderGroup(T object, String group);
	@SideOnly(Side.CLIENT)
	public Optional<TRSRTransformation> applyTransformations(T object, String group, Optional<TRSRTransformation> transform);
	@SideOnly(Side.CLIENT)
	public Matrix4 handlePerspective(T Object, TransformType cameraTransformType, Matrix4 perspective);
}