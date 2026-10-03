# Laboratory Exercise 12 – 2D Game Using 3D Game Engine 2 (04Lab1)

Jump-over-the-obstacle game in Unity 3D. Save as `Play2.unity`.

## Quick way (automatic)

1. Unity Hub → **New project → 2D (Built-in or URP)** → name it `04Lab1`.
2. Copy this folder's `Assets/Scripts` and `Assets/Editor` into your project's `Assets`.
3. Copy the four lab images into `Assets/Images`, named `04 Image 1` … `04 Image 4`
   (Image 1 = background, 2 = player, 3 and 4 = obstacles).
4. Menu **Lab → Build Play2 Scene**. It creates the camera, scrolling background, ground, player,
   two obstacle prefabs, spawner, and saves `Assets/Scenes/Play2.unity`.
5. Press **Play**. Space / Up Arrow / mouse click / tap = jump. Hit an obstacle = Game Over; press jump again to restart.

## Manual way (same result)

1. Import the four images; in the Inspector set **Texture Type = Sprite (2D and UI)** → Apply.
2. Drag **Image 1** into the Hierarchy twice (side by side, scaled to fill the camera height) → add `ScrollingBackground`
   to each (set Tile Width = width of one tile, Tile Count = 2).
3. Create an empty `Ground` object with a `Box Collider 2D` just below the floor of the background (y ≈ -4).
4. Drag **Image 2** in as `Player` → add `Rigidbody2D` (Gravity Scale 3, Freeze Rotation Z), `Box Collider 2D`, `PlayerController`.
5. Drag **Image 3** and **Image 4** in, add `Box Collider 2D` (**Is Trigger** on) + `Obstacle`, drag each into the Project window to make prefabs, delete from the scene.
6. Empty `GameManager` object → `GameManager` script. Empty `ObstacleSpawner` object → `ObstacleSpawner`, put both obstacle prefabs in **Prefabs**.
7. File → Save As → `Play2`.

## Rubric coverage

- **Gameplay (30):** clear goal – survive as long as possible, score rises with time, speed ramps up, best score is kept.
- **Game function (20):** jump only when grounded, collision = game over, restart works, obstacles are cleaned up off-screen, gaps always leave a jumpable window.
