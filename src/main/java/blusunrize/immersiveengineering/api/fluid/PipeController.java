package blusunrize.immersiveengineering.api.fluid;

import java.util.HashSet;

public class PipeController implements IFluidPipe {
  protected HashSet<PipeConnection> connections = new HashSet<PipeConnection>();

  @Override
  public void addConnection(PipeConnection connection) {
    connections.add(connection);
  }
}
