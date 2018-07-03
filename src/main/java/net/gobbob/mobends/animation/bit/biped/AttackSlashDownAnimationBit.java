package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.GUtil;
import net.gobbob.mobends.util.SmoothOrientation;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class AttackSlashDownAnimationBit extends AnimationBit
{
	private float ticksPlayed;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "attack", "attack_1" };
	}

	@Override
	public void onPlay(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;

		BipedEntityData data = (BipedEntityData) entityData;
		data.swordTrail.reset();
		
		this.ticksPlayed = 0F;
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;
		if (!(entityData.getEntity() instanceof EntityLivingBase))
			return;

		BipedEntityData data = (BipedEntityData) entityData;
		EntityLivingBase living = (EntityLivingBase) data.getEntity();
		EnumHandSide primaryHand = living.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;
		ItemStack offHandItemStack = living.getHeldItemOffhand();

		if (living.getHeldItem(EnumHand.MAIN_HAND) != null)
		{
			if (data.getTicksAfterAttack() < 4F &&
				living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
			{
				data.swordTrail.add(data);
			}
		}

		float attackState = this.ticksPlayed / 10F;
		float armSwing = GUtil.clamp(attackState * 3F, 0F, 1F);

		Vector3f bodyRot = new Vector3f(0, 0, 0);
		bodyRot.x = 20F - attackState * 20F;
		bodyRot.y = (30F + 10F * attackState) * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRot.x)
				.orientY(bodyRot.y);
		data.head.rotation.orientX(data.getHeadPitch() - bodyRot.x)
				.rotateY(data.getHeadYaw() - bodyRot.y - 30 * handDirMtp);
		
		mainArm.getRotation().setSmoothness(.3F).orientZ(60F * handDirMtp)
				.rotateInstantY(-20F + armSwing * 70F);
		offArm.getRotation().setSmoothness(.3F).orientZ(-80 * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-20F);
		offForeArm.getRotation().setSmoothness(.3F).orientX(-60F);

		if (data.isStillHorizontally())
		{
			data.rightLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateZ(10)
					.rotateY(25);
			data.leftLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateZ(-10)
					.rotateY(-25);
			
			data.rightForeLeg.rotation.setSmoothness(.3F).orientX(30F);
			data.leftForeLeg.rotation.setSmoothness(.3F).orientX(30F);
		}

		data.renderOffset.slideY(-2F);
		mainItemRotation.orientInstantX(90);
		data.renderRotation.setSmoothness(.3F).orientY(-30 * handDirMtp);

		ticksPlayed += DataUpdateHandler.ticksPerFrame;
	}
}
