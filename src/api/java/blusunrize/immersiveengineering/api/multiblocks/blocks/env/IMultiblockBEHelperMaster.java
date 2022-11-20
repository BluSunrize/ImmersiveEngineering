package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import javax.annotation.Nonnull;

@NonExtendable
public interface IMultiblockBEHelperMaster<State extends IMultiblockState> extends IMultiblockBEHelper<State>
{
	SetRestrictedField<Factory> MAKE_HELPER = SetRestrictedField.common();

	void invalidateCaps();

	@Nonnull
	@Override
	State getState();

	@Nonnull
	@Override
	IMultiblockContext<State> getContext();

	Packet<ClientGamePacketListener> getUpdatePacket();

	interface Factory
	{
		<T extends IMultiblockState>
		IMultiblockBEHelperMaster<T> makeFor(MultiblockBlockEntityMaster<T> be, MultiblockRegistration<T> logic);
	}
}
