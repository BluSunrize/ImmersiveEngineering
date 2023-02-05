/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.PipeCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity.ConnectionStyle;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.math.Transformation;
import org.joml.Vector4f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.core.Direction.*;

public class PipeCallbacks implements BlockCallback<Key>
{
	public static final PipeCallbacks INSTANCE = new PipeCallbacks();

	private static final Key INVALID = new Key(
			Util.make(new EnumMap<>(Direction.class), m -> {
				for(Direction d : DirectionUtils.VALUES)
					m.put(d, ConnectionStyle.NO_CONNECTION);
			}), null, null
	);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof FluidPipeBlockEntity pipeBE))
			return getDefaultKey();
		Map<Direction, ConnectionStyle> connections = new EnumMap<>(Direction.class);
		for(Direction face : DirectionUtils.VALUES)
			connections.put(face, pipeBE.getConnectionStyle(face));
		return new Key(connections, pipeBE.cover==Blocks.AIR?null: pipeBE.cover, pipeBE.getColor());
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public IEObjState getIEOBJState(Key key)
	{
		List<String> parts = new ArrayList<>();
		Matrix4 rotationMatrix = new Matrix4();
		rotationMatrix.translate(0.5, 0.5, 0.5);
		if(key.cover()!=null)
			parts.add("cover");
		int totalConnections = key.numActiveConnections();
		boolean straightY = key.all(UP, DOWN);
		boolean straightZ = key.all(NORTH, SOUTH);
		boolean straightX = key.all(EAST, WEST);
		switch(totalConnections)
		{
			case 0://stub
				parts.add("center");
				break;
			case 1://stopper
				parts.add("stopper");

				//default: y-
				if(key.hasActiveConnection(UP))//y+
					rotationMatrix.rotate(Math.PI, 0, 0, 1);
				else if(key.hasActiveConnection(NORTH))//z-
					rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
				else if(key.hasActiveConnection(SOUTH))//z+
					rotationMatrix.rotate(-Math.PI/2, 1, 0, 0);
				else if(key.hasActiveConnection(WEST))//x-
					rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
				else if(key.hasActiveConnection(EAST))//x+
					rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
				parts.add("con_yMin");
				break;
			case 2://straight or curve
				if(straightY)
				{
					parts.add("pipe_y");
					if(key.hasCouplingConnection(DOWN))
						parts.add("con_yMin");
					if(key.hasCouplingConnection(UP))
						parts.add("con_yMax");
				}
				else if(straightZ)
				{
					parts.add("pipe_z");
					if(key.hasCouplingConnection(NORTH))
						parts.add("con_zMin");
					if(key.hasCouplingConnection(SOUTH))
						parts.add("con_zMax");
				}
				else if(straightX)
				{
					parts.add("pipe_x");
					if(key.hasCouplingConnection(WEST))
						parts.add("con_xMin");
					if(key.hasCouplingConnection(EAST))
						parts.add("con_xMax");
				}
				else
				{
					parts.add("curve");
					parts.add("con_yMin");
					parts.add("con_zMin");
					if(key.any(UP, DOWN))//curve to top or bottom
					{
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
						else if(key.hasActiveConnection(EAST))//x+
							rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
						else if(key.hasActiveConnection(SOUTH))//z+
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
						if(key.hasActiveConnection(UP))//flip to top
							rotationMatrix.rotate(Math.PI, 0, 0, 1);

						//default: Curve to z-
					}
					else//curve to horizontal
					{
						rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
						if(key.all(SOUTH, EAST))//z+ to x+
							rotationMatrix.rotate(Math.PI, 1, 0, 0);
						else if(key.all(SOUTH, WEST))//z+ to x-
							rotationMatrix.rotate(-Math.PI/2, 1, 0, 0);
						else if(key.all(NORTH, EAST))//z- to x+
							rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
						//default: z- to x-
					}
				}
				break;
			case 3://tcross or tcurve
				if(straightX||straightZ||straightY)//has straight connect
				{
					parts.add("tcross");
					parts.add("con_yMin");
					parts.add("con_zMin");
					parts.add("con_zMax");
					if(straightX)
					{
						rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
						if(key.hasActiveConnection(NORTH))//z-
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(SOUTH))//z+
							rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(UP))//y+
							rotationMatrix.rotate(Math.PI, 0, 0, 1);
						//default: Curve to y-
					}
					else if(straightY)
					{
						rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(EAST))//x+
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(SOUTH))//z+
							rotationMatrix.rotate(Math.PI, 0, 0, 1);
						//default: Curve to z-
					}
					else //default:z straight
					{
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(EAST))//x+
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						else if(key.hasActiveConnection(UP))//y+
							rotationMatrix.rotate(Math.PI, 0, 0, 1);
						//default: Curve to y-
					}
				}
				else //tcurve
				{
					parts.add("tcurve");
					parts.add("con_yMin");
					parts.add("con_zMin");
					parts.add("con_xMax");
					//default y-, z-, x+
					if(key.hasActiveConnection(SOUTH))//z+
					{
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
						else
							rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
					}
					else//z-
					{
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
					}
					if(key.hasActiveConnection(UP))//y+
						rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
				}
				break;
			case 4://cross or complex tcross
				boolean cross = (straightX&&straightZ)||(straightX&&straightY)||(straightZ&&straightY);
				if(cross)
				{
					parts.add("cross");
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					if(!straightY)//x and z
						rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
					else if(straightX)//x and y
						rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
				}
				else
				{
					parts.add("tcross2");
					parts.add("con_yMin");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMax");
					if(straightZ)
					{
						//default y- z+- x+
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
						if(key.hasActiveConnection(UP))//y+
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
					}
					else if(straightY)
					{
						rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
						//default y+- z- x+
						if(key.hasActiveConnection(SOUTH))//z+
						{
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
							if(key.hasActiveConnection(WEST))//x-
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						}
						else if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
					}
					else
					{
						rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
						//default y- z- x+-
						if(key.hasActiveConnection(SOUTH))//z+
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
						if(key.hasActiveConnection(UP))//y+
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
					}
				}
				break;
			case 5://complete tcross
				parts.add("tcross3");
				parts.add("con_yMin");
				parts.add("con_yMax");
				parts.add("con_zMin");
				parts.add("con_zMax");
				parts.add("con_xMax");
				//default y+- z+- x+
				if(straightZ)
				{
					if(straightY)
					{
						if(key.hasActiveConnection(WEST))//x-
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
					}
					else if(straightX)
						rotationMatrix.rotate((key.hasActiveConnection(UP))?(Math.PI/2): (-Math.PI/2), 0, 0, 1);
				}
				else if(straightX)
				{
					rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
					if(key.hasActiveConnection(SOUTH))//z+
						rotationMatrix.rotate(Math.PI, 0, 1, 0);
				}
				break;
			case 6://Full Crossing
				parts.add("con_yMin");
				parts.add("con_yMax");
				parts.add("con_zMin");
				parts.add("con_zMax");
				parts.add("con_xMin");
				parts.add("con_xMax");

				break;
		}
		rotationMatrix.translate(-0.5, -0.5, -0.5);

		return new IEObjState(VisibilityList.show(parts), new Transformation(rotationMatrix.toMatrix4f()));
	}

	@Override
	public List<BakedQuad> modifyQuads(Key key, List<BakedQuad> quads)
	{
		if(key.cover()!=null)
		{
			BlockState state = key.cover().defaultBlockState();
			BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
			for(RenderType layer : RenderType.chunkBufferLayers())
			{
				for(Direction direction : Direction.values())
					quads.addAll(model.getQuads(state, direction, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, layer));
				quads.addAll(model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, layer));
			}
		}
		return quads;
	}

	@Override
	public Vector4f getRenderColor(Key key, String group, String material, ShaderCase shaderCase, Vector4f original)
	{
		if(key.color()!=null)
		{
			float[] rgb = key.color().getTextureDiffuseColors();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		return original;
	}


	public record Key(
			Map<Direction, ConnectionStyle> connections,
			@Nullable Block cover,
			@Nullable DyeColor color
	)
	{
		int numActiveConnections()
		{
			int count = 0;
			for(ConnectionStyle c : connections.values())
				if(c!=ConnectionStyle.NO_CONNECTION)
					count++;
			return count;
		}

		public boolean hasActiveConnection(Direction side)
		{
			return connections.get(side)!=ConnectionStyle.NO_CONNECTION;
		}

		public boolean hasCouplingConnection(Direction side)
		{
			return connections.get(side)==ConnectionStyle.FLANGE;
		}

		public boolean any(Direction... sides)
		{
			for(Direction side : sides)
				if(hasActiveConnection(side))
					return true;
			return false;
		}

		public boolean all(Direction... sides)
		{
			for(Direction side : sides)
				if(!hasActiveConnection(side))
					return false;
			return true;
		}
	}
}
