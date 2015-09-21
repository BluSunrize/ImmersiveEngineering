package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.fluid.PipeConnection;
import blusunrize.immersiveengineering.api.fluid.PipeController;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityFluidPump extends TileEntityIEBase implements IFluidPipe {
  public int[] sideConfig = new int[] { 0, -1, 0, 0, 0, 0 };
  public boolean dummy = true;
  public PipeController pipeController = new PipeController();

  @Override
  public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
    sideConfig = nbt.getIntArray("SideConfig");
    if (sideConfig == null || sideConfig.length != 6) {
      sideConfig = new int[] { 0, -1, 0, 0, 0, 0 };
    }
    dummy = nbt.getBoolean("Dummy");
    if (descPacket) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  @Override
  public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
    nbt.setIntArray("SideConfig", sideConfig);
    nbt.setBoolean("Dummy", dummy);
  }

  public void toggleSide(int side) {
    if (side != 1) {
      sideConfig[side]++;
      if (sideConfig[side] > 2) {
        sideConfig[side] = 0;
      }
    }
  }


  ////
  // IFluidPipe
  ////

  @Override
  public void addConnection(PipeConnection connection) {
    pipeController.addConnection(connection);
  }
}
