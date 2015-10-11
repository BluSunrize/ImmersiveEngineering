package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.fluid.PipeConnection;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe_old;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;

public class TileRenderFluidPipe_old extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/pipe.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_fluidPipe);
		}
	};

	static String[] CONNECTIONS = new String[]{
		"con_yMin", "con_yMax", "con_zMin", "con_zMax", "con_xMin", "con_xMax"
	};

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);

		if(tile.getWorldObj() == null)
		{
			translationMatrix.translate(0, -0.1, 0);
			model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, "con_yMin", "con_yMax", "pipe_y");
		}
		else
		{
			ArrayList<String> parts = new ArrayList<String>();
			ArrayList<String> conParts = new ArrayList<String>();

			byte connections = 0;
			for(PipeConnection connection : ((TileEntityFluidPipe_old) tile).connections)
			{
				if(connection.direction != ForgeDirection.UNKNOWN)
				{
					connections = (byte) (connections | 1 << connection.direction.ordinal());
					if(connection.type == PipeConnection.Type.TANK)
					{
						if(!conParts.contains(CONNECTIONS[connection.direction.ordinal()]))
							conParts.add(CONNECTIONS[connection.direction.ordinal()]);
					}
					else
					{
						TileEntity tileEntity = tile.getWorldObj().getTileEntity(tile.xCoord + connection.direction.offsetX, tile.yCoord + connection.direction.offsetY, tile.zCoord + connection.direction.offsetZ);
						if(tileEntity instanceof TileEntityFluidPipe_old)
						{
							byte cons = ((TileEntityFluidPipe_old) tileEntity).getConnections();
							if (((TileEntityFluidPipe_old) tileEntity).connections.size() > 1)
								if(cons!=48 && cons!=12 && cons!=3)
									if (!conParts.contains(CONNECTIONS[connection.direction.ordinal()]))
										conParts.add(CONNECTIONS[connection.direction.ordinal()]);
						}
					}
				}
			}


			if(((TileEntityFluidPipe_old) tile).connections.size()==0)
			{
				parts.clear();
				conParts.clear();
				parts.add("pipe_y");
				conParts.add(CONNECTIONS[0]);
				conParts.add(CONNECTIONS[1]);
				model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, conParts.toArray(new String[conParts.size()]));
			}
			else
			{
				if(((TileEntityFluidPipe_old) tile).connections.size()==1)
				{
					byte tempCon = connections;
					for(int i = 0; i < 6; i++)
						if((tempCon & (1 << i)) >> i == 1)
						{
							connections = (byte) (connections | 1 << ForgeDirection.OPPOSITES[i]);
							conParts.add(CONNECTIONS[ForgeDirection.OPPOSITES[i]]);
						}
				}

				if(connections == 48) // 110000
					parts.add("pipe_x");
				else if(connections == 12) // 001100
					parts.add("pipe_z");
				else if(connections == 3) // 000011
					parts.add("pipe_y");
				else
					parts.add("center");

				if(conParts.size() > 0 && (connections == 48 || connections == 12 || connections == 3))
					model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, conParts.toArray(new String[conParts.size()]));
			}

			model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, parts.toArray(new String[parts.size()]));
		}
	}
}
