package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.Optional;

import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.WireType;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "OpenComputers"),
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
})
public class TileEntityConnectorTelecommunication extends TileEntityImmersiveConnectable implements SidedEnvironment, Environment{
	NodeContener nodeContener;

    public int facing;
    
    public static boolean isOCLoaded = false;
    
    private class NodeContener{
    	private Node node;
        private boolean addedToNetwork = false;
        TileEntityConnectorTelecommunication te;
    	public NodeContener(TileEntityConnectorTelecommunication tile) {
    		node = Network.newNode(tile, Visibility.Network).create();
    		te = tile;
		}
    	
    	public Node getNode() {
			return node;
		}
    	
    	public void writeToNBT(final NBTTagCompound nbt){
            if (node != null && node.host() == te) {
                final NBTTagCompound nodeNbt = new NBTTagCompound();
                node.save(nodeNbt);
                nbt.setTag("oc:node", nodeNbt);
            }
    	}
    	
    	public void readFromNBT(final NBTTagCompound nbt) {
            if (node != null && node.host() == te) {
                node.load(nbt.getCompoundTag("oc:node"));
            }
    	}
    	
    	public void invalidate(){
    		if (node != null) node.remove();
    	}
    	
    	public void onChunkUnload(){
    		if (node != null) node.remove();
    	}
    	
    	public void update(){
            if (!addedToNetwork) {
                addedToNetwork = true;
                Network.joinOrCreateNetwork(te);
            }
    	}
    	
    	public void removeConnection(Connection con){
    		if(con.start.equals(Utils.toCC(te))){
    			if(CCToTileEntity(con.end)!=null && node!=null){
    				node.disconnect(CCToTileEntity(con.end).node());
    			}
    		}
    	}
    	
    	public void checkConnections(){
    		List<Connection> a = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, Utils.toCC(te));
    		for(Connection s : a){
    			if(s.start.equals(Utils.toCC(te)) && CCToTileEntity(s.end)!=null){
    				Node n = CCToTileEntity(s.end).node();
    				if(!node.isNeighborOf(n)){
    					node.connect(n);
    				}
    			}
    		}
    	}
    	
    }
    
    public TileEntityConnectorTelecommunication() {
    	if(isOCLoaded){
    		nodeContener = new NodeContener(this);
    	}
    	this.limitType = WireType.TELECOMMUNICATION;
	}
    
    @Override
    protected boolean canTakeLV() {
    	return false;
    }
    
    @Override
    protected boolean canTakeMV() {
    	return false;
    }
    
    @Override
    protected boolean canTakeHV() {
    	return false;
    }
    
	@Override
	protected boolean canProvideOCPacket() {
		return true;
	}
	
	@Override
	public Vec3 getRaytraceOffset()
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		return Vec3.createVectorHelper(.5+.5*fd.offsetX, .5+.5*fd.offsetY, .5+.5*fd.offsetZ);
	}
	
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing).getOpposite();
		double conRadius = .03125;
		return Vec3.createVectorHelper(.5-conRadius*fd.offsetX, .5-conRadius*fd.offsetY, .5-conRadius*fd.offsetZ);	
	}

	@Override
	public void connectCable(WireType wiretype, TargetingInfo target) {
		if(isOCLoaded){
			nodeContener.checkConnections();	
		}
	}
	
	@Override
	public void removeCable(Connection con) {
		if(isOCLoaded){
			nodeContener.removeConnection(con);
		}
	}

	public TileEntityConnectorTelecommunication CCToTileEntity(ChunkCoordinates cc){
		TileEntity te  = worldObj.getTileEntity(cc.posX, cc.posY, cc.posZ);
		if(te instanceof TileEntityConnectorTelecommunication){
			return (TileEntityConnectorTelecommunication)te;
		}else{
			return null;
		}
	}
	
	@Override
	public boolean isEnergyOutput() {
		return false;
	}

	@Override
	public int outputEnergy(int arg0, boolean arg1, int arg2) {
		return 0;
	}

	@Override
	public boolean canConnect() {
		return true;
	}
	
    @Override
    public void invalidate() {
        super.invalidate();
        if(isOCLoaded){
        	nodeContener.invalidate();
        }    
    }
	
    /* OC */
    @Optional.Method(modid = "OpenComputers")
	@Override
	public Node node() {
		return nodeContener.getNode();
	}
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public void onConnect(Node node) {
    	if(isOCLoaded){
    		nodeContener.checkConnections();
    	}
	}
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public void onDisconnect(Node node) {
		
	}
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public void onMessage(Message message) {
		
	}
	
    @Override
    public void updateEntity() {
    	if(isOCLoaded){
    		nodeContener.update();
    	}
    }

    @Override
    public boolean canUpdate() {
    	return isOCLoaded;
    }
    
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if(isOCLoaded){
        	nodeContener.onChunkUnload();
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if(nbt.hasKey("facing")){
        	facing = nbt.getInteger("facing");
        }
        if(isOCLoaded){
        	nodeContener.readFromNBT(nbt);
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("facing", facing);
        if(isOCLoaded){
        	nodeContener.writeToNBT(nbt);
        }
    }
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public boolean canConnect(ForgeDirection direction) {
		return direction==ForgeDirection.getOrientation(facing);
	}
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public Node sidedNode(ForgeDirection direction) {
		return node();
	}

}
