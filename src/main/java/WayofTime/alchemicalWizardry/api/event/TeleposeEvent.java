package WayofTime.alchemicalWizardry.api.event;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/** Fired when a teleposer attempts to transpose two blocks. Use this to perform special cleanup or compensation,
or cancel it entirely to prevent the transposition. */
@Cancelable
public class TeleposeEvent extends Event {
  
  public final World initialWorld;
  public final int initialX;
  public final int initialY;
  public final int initialZ;
  
  public final Block initialBlock;
  public final int initialMetadata;
  
  public final World finalWorld;
  public final int finalX;
  public final int finalY;
  public final int finalZ;
  
  public final Block finalBlock;
  public final int finalMetadata;
  
  public TeleposeEvent(World wi, int xi, int yi, int zi, Block bi, int mi, World wf, int xf, int yf, int zf, Block bf, int mf) {
    initialWorld = wi;
    initialX = xi;
    initialY = yi;
    initialZ = zi;
    
    initialBlock = bi;
    initialMetadata = mi;
    
    finalWorld = wf;
    finalX = xf;
    finalY = yf;
    finalZ = zf;
    
    finalBlock = bf;
    finalMetadata = mf;
  }
  
  public TileEntity getInitialTile() {
    return initialWorld.getTileEntity(initialX, initialY, initialZ);
  }
  
  public TileEntity getFinalTile() {
    return finalWorld.getTileEntity(finalX, finalY, finalZ);
  }

}
