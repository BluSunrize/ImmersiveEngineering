package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorCacheData;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorWall;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorModelRender;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.RedstoneConveyor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Lib.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class RedstoneConveyorRender extends BasicConveyorRender<RedstoneConveyor>
{
	public static final ResourceLocation texture_panel = new ResourceLocation("immersiveengineering:block/conveyor/redstone");
	public static final String MODEL_NAME = "conveyor_redstone_panel";
	public static DynamicModel MODEL_PANEL;

	private static final Map<Direction, BakedModel> ROTATED_MODELS = new EnumMap<>(Direction.class);

	@SubscribeEvent
	public static void initModels(ModelEvent.BakingCompleted ev)
	{
		ResourceLocation modelName = MODEL_PANEL.getName();
		UnbakedModel model = ev.getModelBakery().getModel(modelName);
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			ModelState transform = BlockModelRotation.by(0, (int)d.toYRot()+180);
			BakedModel baked = model.bake(
					ev.getModelBakery(), ev.getModelBakery().getAtlasSet()::getSprite, transform, modelName
			);
			ROTATED_MODELS.put(d, baked);
		}
	}

	public RedstoneConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		super(active, inactive);
	}

	@Override
	public Object getModelCacheKey(RenderContext<RedstoneConveyor> context)
	{
		BasicConveyorCacheData basic = IConveyorModelRender.getDefaultData(this, context);
		RedstoneConveyor instance = context.instance();
		if(instance==null)
			return basic;
		return Pair.of(basic, instance.isPanelRight());
	}

	@Override
	public boolean shouldRenderWall(Direction facing, ConveyorWall wall, RenderContext<RedstoneConveyor> context)
	{
		RedstoneConveyor instance = context.instance();
		if(instance==null||(instance.isPanelRight()&&wall==ConveyorWall.RIGHT)||(!instance.isPanelRight()&&wall==ConveyorWall.LEFT))
			return true;
		return super.shouldRenderWall(facing, wall, context);
	}

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, RenderContext<RedstoneConveyor> context, @Nullable RenderType renderType)
	{
		if(renderType!=null&&renderType!=RenderType.cutout())
			return super.modifyQuads(baseModel, context, renderType);
		boolean panelRight = context.instance()==null||context.instance().isPanelRight();
		Direction facing = context.getFacing();
		BakedModel model = ROTATED_MODELS.get(panelRight?facing: facing.getOpposite());
		if(model!=null)
		{
			String[] parts = context.isActiveOr(false)?new String[]{"panel", "lamp"}: new String[]{"panel"};
			baseModel.addAll(model.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelDataUtils.single(
					DynamicSubmodelCallbacks.getProperty(), VisibilityList.show(parts)
			), null));
		}
		return baseModel;
	}
}
