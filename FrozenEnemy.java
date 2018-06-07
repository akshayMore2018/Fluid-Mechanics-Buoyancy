package com.mygdx.game;


class FrozenEnemy implements Behavior
{
    private Enemy enemy;
    private float waterLevel;
    private float objectHeight;
    //forces
    private float buoyantForce;
    private float weightForce;
    //densities
    private float fluidDensity;
    private float objectDensity;
    //gravitational pull
    private final float G = 0.2f;
    //damping factor to slow down velocity.
    private float damping;

    private boolean isPlayerOnTop;
    private float incrementAngle;

    @Override
    public void init(Enemy enemy)
    {
        this.enemy = enemy;
        isPlayerOnTop = false;
        initForces();
    }

    private void initForces()
    {
        waterLevel = enemy.currentWater.top;
        objectHeight = enemy.collision.getHeight();
        enemy.velocity.y = 4;
        objectDensity = 1.5f;
        fluidDensity = 2.8f;
        weightForce = G * objectDensity;
        buoyantForce = -G * fluidDensity * (volumeSubmerged() / objectHeight);
        damping = 1;
        incrementAngle = 1.8f;
    }

    private float volumeSubmerged()
    {
        float partSubmerged;
        if (enemy.top >= waterLevel)
        {
            partSubmerged = objectHeight;
        }
        else if (enemy.bottom < waterLevel)
        {
            partSubmerged = 0;
        }
        else
        {
            partSubmerged = enemy.bottom - waterLevel;
        }
        return partSubmerged;
    }

    private void fluidMechanics()
    {
        if (enemy.position.y <= waterLevel)
        {
            damping = 0.5f;  //damping applied to slow down floating(up-down motion) at surface.
        }
        else if (enemy.top >= waterLevel)
        {
            damping = 1;   //damping=1, does not have any effect, let the object move up due to buoyancy.
        }
        enemy.velocity.y += weightForce + buoyantForce; //net acceleration due to opposing forces.
        if (enemy.velocity.y < -3)
            enemy.velocity.y = -3;
        buoyantForce = (volumeSubmerged() / objectHeight) * fluidDensity * -G;
        enemy.position.y += enemy.velocity.y * damping;

        horizontalMotion(enemy.position, 1, incrementAngle);
    }

    private void horizontalMotion(Vector2 centre, float radiusX, float incrementAngle)
    {
        //giving a wobble effect while the cube moves up.
        enemy.velocity.x = Utility.getCos(enemy.movementAngle);
        enemy.position.x = centre.x + enemy.velocity.x * radiusX;
        enemy.movementAngle += incrementAngle;
    }

    @Override
    public void execute()
    {
        fluidMechanics();

        if (Player.currentState.ID == PlayerStateJump.getInstance().ID)
        {
            //The floating frozen enemy is pushed down a bit when player lands on it.
            //This should only happen when player jumps on the floating frozen enemy.
            isPlayerOnTop = false;
        }
    }

    @Override
    public void reset()
    {
        isPlayerOnTop = false;
        if (!enemy.isWaterEnemy)
        {
           enemy.die();
        }
    }

    @Override
    public void onCollision(GameObject gameObject)
    {
        if (gameObject.name == "player")
        {
            if (!isPlayerOnTop)
            {
                if (gameObject.velocity.y > 0)
                    enemy.velocity.y = 6;
                isPlayerOnTop = true;
            }
        }
    }
}


interface Behavior{
    void init(Enemy enemy);
    void execute();
    void reset();
    void onCollision(GameObject gameObject)

}