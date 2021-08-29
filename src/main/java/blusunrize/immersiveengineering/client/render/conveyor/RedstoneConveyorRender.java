package blusunrize.immersiveengineering.client.render.conveyor;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.RedstoneConveyor;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneConveyorRender extends BasicConveyorRender<RedstoneConveyor>
{
	public static final ResourceLocation texture_panel = new ResourceLocation("immersiveengineering:block/conveyor/redstone");
	public static final String MODEL_NAME = "conveyor_redstone_panel";
	public static DynamicModel MODEL_PANEL;

	public RedstoneConveyorRender(ResourceLocation active, ResourceLocation inactive)
	{
		super(active, inactive);
	}

	@Override
	public String getModelCacheKey(IConveyorType<RedstoneConveyor> type, @Nullable RedstoneConveyor instance)
	{
		String key = super.getModelCacheKey(type, instance);
		if(instance!=null)
			key += "p"+instance.isPanelRight();
		return key;
	}

	@Override
	public boolean renderWall(Direction facing, int wall, @Nullable RedstoneConveyor instance)
	{
		if(instance==null||(instance.isPanelRight()&&wall==1)||(!instance.isPanelRight()&&wall==0))
			return true;
		return super.renderWall(facing, wall, instance);
	}

	@Override
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel, IConveyorType<RedstoneConveyor> type, @Nullable RedstoneConveyor conveyor)
	{
		BakedModel model = MODEL_PANEL.get();
		if(model!=null)
		{
			String[] parts = (conveyor!=null&&!conveyor.isActive())?new String[]{"panel", "lamp"}: new String[]{"panel"};
			IEObjState objState = new IEObjState(VisibilityList.show(parts));
			BlockState state = ConveyorHandler.getBlock(RedstoneConveyor.TYPE).defaultBlockState();
			baseModel.addAll(model.getQuads(state, null, Utils.RAND,
					new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE)));
		}
		return baseModel;
	}
}
