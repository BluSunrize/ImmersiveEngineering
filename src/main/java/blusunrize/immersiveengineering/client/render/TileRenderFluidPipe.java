package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderFluidPipe extends TileRenderIE
{
	static ModelIEObj model = new ModelIEObj("immersiveengineering:models/pipe.obj")
	{
		@Override
		public IIcon getBlockIcon()
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
		if(tile.getWorldObj() == null)
			model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, "con_yMin", "con_yMax", "pipe_y");
		else
		{
			translationMatrix.translate(.5, .5, .5);
			ArrayList<String> parts = new ArrayList<String>();
			ArrayList<String> connectionCaps = new ArrayList<String>();
			byte connections = ((TileEntityFluidPipe)tile).getConnectionByte();
			int totalConnections = Integer.bitCount(connections);
			boolean straightY = (connections&3)==3;
			boolean straightZ = (connections&12)==12;
			boolean straightX = (connections&48)==48;
			switch(totalConnections)
			{
			case 0://stub
				parts.add("center");
				break;
			case 1://stopper
				parts.add("stopper");

				//default: y-
				if((connections&2)!=0)//y+
					rotationMatrix.rotate(Math.PI, 0,0,1);
				else if((connections&4)!=0)//z-
					rotationMatrix.rotate(Math.PI/2, 1,0,0);
				else if((connections&8)!=0)//z+
					rotationMatrix.rotate(-Math.PI/2, 1,0,0);
				else if((connections&16)!=0)//x-
					rotationMatrix.rotate(-Math.PI/2, 0,0,1);
				else if((connections&32)!=0)//x+
					rotationMatrix.rotate(Math.PI/2, 0,0,1);
				break;
			case 2://straight or curve
				if(straightY)
					parts.add("pipe_y");
				else if(straightZ)
					parts.add("pipe_z");
				else if(straightX)
					parts.add("pipe_x");
				else
				{
					parts.add("curve");
					byte connectTo = (byte)(connections&60);
					if((connections&3)!=0)//curve to top or bottom
					{
						if(connectTo==16)//x-
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
						else if(connectTo==32)//x+
							rotationMatrix.rotate(-Math.PI/2, 0,1,0);
						else if(connectTo==8)//z+
							rotationMatrix.rotate(Math.PI, 0,1,0);
						if((connections&2)!=0)//flip to top 
							rotationMatrix.rotate(Math.PI, 0,0,1);
						//default: Curve to z-
					}
					else//curve to horizontal
					{
						rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						if(connectTo==40)//z+ to x+
							rotationMatrix.rotate(Math.PI, 1,0,0);
						else if(connectTo==24)//z+ to x-
							rotationMatrix.rotate(-Math.PI/2, 1,0,0);
						else if(connectTo==36)//x+ to x+
							rotationMatrix.rotate(Math.PI/2, 1,0,0);
						//default: z- to x-
					}
				}
				break;
			case 3://tcross or tcurve
				if(straightX||straightZ||straightY)//has straight connect
				{
					parts.add("tcross");
					if(straightX)
					{
						rotationMatrix.rotate(Math.PI/2, 0,1,0);
						if((connections&4)!=0)//z-
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
						else if((connections&8)!=0)//z+
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						else if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI, 0,0,1);
						//default: Curve to y-
					}
					else if(straightY)
					{
						rotationMatrix.rotate(Math.PI/2, 1,0,0);
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						else if((connections&32)!=0)//x+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
						else if((connections&8)!=0)//z+
							rotationMatrix.rotate(Math.PI, 0,0,1);
						//default: Curve to z-
					}
					else //default:z straight
					{
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
						else if((connections&32)!=0)//x+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
						else if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI, 0,0,1);
						//default: Curve to y-
					}
				}
				else //tcurve
				{
					parts.add("tcurve");
					//default y-, z-, x+
					if((connections&8)!=0)//z+
					{
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(Math.PI, 0,1,0);
						else 
							rotationMatrix.rotate(-Math.PI/2, 0,1,0);
					}
					else//z-
					{
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(Math.PI/2, 0,1,0);
					}
					if((connections&2)!=0)//y+
						rotationMatrix.rotate(Math.PI/2, 0,0,1);
				}
				break;
			case 4://cross or complex tcross
				boolean cross = (straightX&&straightZ)||(straightX&&straightY)||(straightZ&&straightY);
				if(cross)
				{
					parts.add("cross");
					if(!straightY)//x and z
						rotationMatrix.rotate(Math.PI/2, 0,0,1);
					else if(straightX)//x and y
						rotationMatrix.rotate(Math.PI/2, 0,1,0);
				}
				else
				{
					parts.add("tcross2");
					if(straightZ)
					{
						//default y- z+- x+
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(Math.PI, 0,1,0);
						if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
					}
					else if(straightY)
					{
						//default y+- z- x+
						if((connections&8)!=0)//z+
						{
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI/2, 0,0,1);
						}
						else if((connections&16)!=0)//x-
							rotationMatrix.rotate(-Math.PI/2, 0,0,1);
					}
					else
					{
						rotationMatrix.rotate(Math.PI/2, 0,1,0);
						//default y- z- x+-
						if((connections&8)!=0)//z+
							rotationMatrix.rotate(Math.PI, 0,1,0);
						if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI/2, 0,0,1);
					}
				}
				break;
			case 5://complete tcross
				parts.add("tcross3");
				//default y+- z+- x+
				if(straightZ)
				{
					if(straightY)
					{
						if((connections&16)!=0)//x-
							rotationMatrix.rotate(Math.PI, 0,1,0);
					}
					else if(straightX)
						rotationMatrix.rotate(((connections&2)!=0)?(Math.PI/2):(-Math.PI/2), 0,0,1);
				}
				else if(straightX)
				{
					rotationMatrix.rotate(Math.PI/2, 0,1,0);
					if((connections&8)!=0)//z+
						rotationMatrix.rotate(Math.PI, 0,1,0);
				}
				break;
			case 6://Full Crossing
				break;
			}

			for(int i=0; i<6; i++)
				if(((TileEntityFluidPipe)tile).getConnectionStyle(i)==1)
					connectionCaps.add(CONNECTIONS[i]);



			if(!parts.isEmpty())
				model.render(tile, tes, translationMatrix, rotationMatrix, 1, false, parts.toArray(new String[parts.size()]));
			if(!connectionCaps.isEmpty())
				model.render(tile, tes, translationMatrix, new Matrix4(), 1, false, connectionCaps.toArray(new String[connectionCaps.size()]));
		}
	}
}
