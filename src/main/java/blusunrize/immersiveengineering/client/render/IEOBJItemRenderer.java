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
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends BlockEntityWithoutLevelRenderer
{
	public static final Supplier<BlockEntityWithoutLevelRenderer> INSTANCE = Suppliers.memoize(
			() -> new IEOBJItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()
			)
	);
	public static final IItemRenderProperties USE_IEOBJ_RENDER = new IItemRenderProperties()
	{
		@Override
		public BlockEntityWithoutLevelRenderer getItemStackRenderer()
		{
			return INSTANCE.get();
		}
	};

	public IEOBJItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_)
	{
		super(p_172550_, p_172551_);
	}

	@Override
	public void renderByItem(ItemStack stack, TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn,
							 int combinedLightIn, int combinedOverlayIn)
	{
		float partialTicks = mc().getFrameTime();
		if(stack.getItem() instanceof IOBJModelCallback)
		{
			IOBJModelCallback<ItemStack> callback = (IOBJModelCallback<ItemStack>)stack.getItem();
			Level w = IESmartObjModel.tempEntityStatic!=null?IESmartObjModel.tempEntityStatic.level: null;
			BakedModel model = mc().getItemRenderer().getModel(stack, w, IESmartObjModel.tempEntityStatic, 0);
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
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					Transformation mat = callback.getTransformForGroups(stack, groups, transformType, mc().player,
							partialTicks);
					mat.push(matrixStackIn);
					renderQuadsForGroups(groups, callback, obj, stack, sCase, matrixStackIn, bufferIn, visible,
							combinedLightIn, combinedOverlayIn);
					matrixStackIn.popPose();
				}
				renderQuadsForGroups(visible.toArray(new String[0]), callback, obj, stack,
						sCase, matrixStackIn, bufferIn, visible, combinedLightIn, combinedOverlayIn);
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  ItemStack stack, ShaderCase sCase, PoseStack matrix, MultiBufferSource buffer,
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
		matrix.pushPose();
		for(ShadedQuads quadsForLayer : quadsByLayer)
		{
			boolean bright = callback.areGroupsFullbright(stack, groups);
			RenderType baseType;
			ResourceLocation atlas = InventoryMenu.BLOCK_ATLAS;
			if(bright)
				baseType = IERenderTypes.getFullbrightTranslucent(atlas);
			else if(quadsForLayer.layer.isTranslucent())
				baseType = RenderType.entityTranslucent(atlas);
			else
				baseType = RenderType.entityCutout(atlas);
			RenderType actualType = quadsForLayer.layer.getRenderType(baseType);
			VertexConsumer builder = IERenderTypes.disableCull(buffer).getBuffer(actualType);
			Vector4f color = quadsForLayer.layer.getColor();
			for(BakedQuad quad : quadsForLayer.quadsInLayer)
				builder.putBulkData(
						matrix.last(), quad, color.x(), color.y(), color.z(), color.w(), light, overlay
				);
			matrix.scale(1.01F, 1.01F, 1.01F);
		}
		matrix.popPose();
	}
}
