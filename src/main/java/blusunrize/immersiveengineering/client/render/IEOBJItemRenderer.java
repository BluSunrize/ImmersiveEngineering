/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel.ShadedQuads;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends ItemStackTileEntityRenderer
{
	public static final ItemStackTileEntityRenderer INSTANCE = new IEOBJItemRenderer();

	@Override
	public void render(ItemStack stack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
					   int combinedLightIn, int combinedOverlayIn)
	{
		float partialTicks = mc().getRenderPartialTicks();
		if(stack.getItem() instanceof IOBJModelCallback)
		{
			IOBJModelCallback<ItemStack> callback = (IOBJModelCallback<ItemStack>)stack.getItem();
			World w = IESmartObjModel.tempEntityStatic!=null?IESmartObjModel.tempEntityStatic.world: null;
			IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, w,
					IESmartObjModel.tempEntityStatic);
			if(model instanceof IESmartObjModel)
			{

				ItemStack shader;
				ShaderCase sCase;
				{
					Pair<ItemStack, ShaderCase> tmp = stack.getCapability(CapabilityShader.SHADER_CAPABILITY)
							.map(wrapper ->
							{
								ItemStack shaderInner = wrapper.getShaderItem();
								ShaderCase sCaseInner = null;
								if(!shaderInner.isEmpty()&&shaderInner.getItem() instanceof IShaderItem)
									sCaseInner = ((IShaderItem)shaderInner.getItem()).getShaderCase(shaderInner, stack, wrapper.getShaderType());
								return new ImmutablePair<>(shaderInner, sCaseInner);
							})
							.orElse(new ImmutablePair<>(ItemStack.EMPTY, null));
					shader = tmp.getLeft();
					sCase = tmp.getRight();
				}
				IESmartObjModel obj = (IESmartObjModel)model;
				Set<String> visible = new HashSet<>();
				for(String g : OBJHelper.getGroups(obj.baseModel).keySet())
					if(callback.shouldRenderGroup(stack, g))
						visible.add(g);
				TransformType transformType = obj.lastCameraTransform;
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					TransformationMatrix mat = callback.getTransformForGroups(stack, groups, transformType, mc().player,
							partialTicks);
					mat.push(matrixStackIn);
					renderQuadsForGroups(groups, callback, obj, stack, sCase, matrixStackIn, bufferIn, visible,
							combinedLightIn, combinedOverlayIn);
					matrixStackIn.pop();
				}
				renderQuadsForGroups(visible.toArray(new String[0]), callback, obj, stack,
						sCase, matrixStackIn, bufferIn, visible, combinedLightIn, combinedOverlayIn);
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  ItemStack stack, ShaderCase sCase, MatrixStack matrix, IRenderTypeBuffer buffer,
									  Set<String> visible, int light, int overlay)
	{
		List<ShadedQuads> quadsByLayer = new ArrayList<>();
		for(String g : groups)
		{
			if(visible.contains(g)&&callback.shouldRenderGroup(stack, g))
				quadsByLayer.addAll(model.addQuadsForGroup(callback, stack, g, sCase, true)
						.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			visible.remove(g);
		}
		matrix.push();
		for(ShadedQuads quadsForLayer : quadsByLayer)
		{
			boolean bright = callback.areGroupsFullbright(stack, groups);
			RenderType baseType;
			ResourceLocation atlas = PlayerContainer.LOCATION_BLOCKS_TEXTURE;
			if(bright)
				baseType = IERenderTypes.getFullbrightTranslucent(atlas);
			else if(quadsForLayer.layer.isTranslucent())
				baseType = RenderType.getEntityTranslucent(atlas);
			else
				baseType = RenderType.getEntityCutout(atlas);
			RenderType actualType = quadsForLayer.layer.getRenderType(baseType);
			IVertexBuilder builder = IERenderTypes.disableCull(buffer).getBuffer(actualType);
			Vector4f color = quadsForLayer.layer.getColor();
			for(BakedQuad quad : quadsForLayer.quadsInLayer)
				builder.addVertexData(
						matrix.getLast(), quad, color.getX(), color.getY(), color.getZ(), color.getW(), light, overlay
				);
			matrix.scale(1.01F, 1.01F, 1.01F);
		}
		matrix.pop();
	}
}
