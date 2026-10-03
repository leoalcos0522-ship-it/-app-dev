using System.IO;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;

// Menu: Lab > Build Play2 Scene
// Builds the whole game scene from the four lab images and saves it as Assets/Scenes/Play2.unity.
public static class BuildPlay2Scene
{
    const float GroundY = -3.5f;

    [MenuItem("Lab/Build Play2 Scene")]
    public static void Build()
    {
        Sprite bg = LoadSprite("04 Image 1");
        Sprite player = LoadSprite("04 Image 2");
        Sprite ob1 = LoadSprite("04 Image 3");
        Sprite ob2 = LoadSprite("04 Image 4");
        if (bg == null || player == null || ob1 == null || ob2 == null)
        {
            EditorUtility.DisplayDialog("Play2",
                "Put '04 Image 1' to '04 Image 4' (png/jpg) inside Assets/Images first.", "OK");
            return;
        }

        Directory.CreateDirectory("Assets/Prefabs");
        Directory.CreateDirectory("Assets/Scenes");
        AssetDatabase.Refresh();

        EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);

        // camera
        var camGo = new GameObject("Main Camera") { tag = "MainCamera" };
        var cam = camGo.AddComponent<Camera>();
        cam.orthographic = true;
        cam.orthographicSize = 5f;
        cam.clearFlags = CameraClearFlags.SolidColor;
        cam.backgroundColor = Color.black;
        camGo.transform.position = new Vector3(0, 0, -10);
        camGo.AddComponent<AudioListener>();

        // game manager
        new GameObject("GameManager").AddComponent<GameManager>();

        // background: a row of tiles scaled to the 10-unit camera height
        float bgScale = 10f / bg.bounds.size.y;
        float tileW = bg.bounds.size.x * bgScale;
        int tiles = Mathf.CeilToInt(24f / tileW) + 2;
        for (int i = 0; i < tiles; i++)
        {
            var t = new GameObject("Background " + (i + 1));
            var sr = t.AddComponent<SpriteRenderer>();
            sr.sprite = bg;
            sr.sortingOrder = -10;
            t.transform.localScale = Vector3.one * bgScale;
            t.transform.position = new Vector3(-tileW + i * tileW, 0, 0);
            var scroll = t.AddComponent<ScrollingBackground>();
            scroll.tileWidth = tileW;
            scroll.tileCount = tiles;
        }

        // invisible ground
        var ground = new GameObject("Ground");
        ground.transform.position = new Vector3(0, GroundY - 0.5f, 0);
        ground.AddComponent<BoxCollider2D>().size = new Vector2(100f, 1f);

        // player
        float pScale = 1.6f / player.bounds.size.y;
        var p = new GameObject("Player");
        var psr = p.AddComponent<SpriteRenderer>();
        psr.sprite = player;
        psr.sortingOrder = 5;
        p.transform.localScale = Vector3.one * pScale;
        p.transform.position = new Vector3(-5f, GroundY + 0.8f + 0.1f, 0);
        var box = p.AddComponent<BoxCollider2D>();
        box.size *= 0.8f;
        var rb = p.AddComponent<Rigidbody2D>();
        rb.gravityScale = 3f;
        rb.freezeRotation = true;
        p.AddComponent<PlayerController>();

        // obstacles
        var spawnerGo = new GameObject("ObstacleSpawner");
        var spawner = spawnerGo.AddComponent<ObstacleSpawner>();
        spawner.prefabs = new[] { MakeObstaclePrefab("Obstacle1", ob1), MakeObstaclePrefab("Obstacle2", ob2) };

        const string scenePath = "Assets/Scenes/Play2.unity";
        EditorSceneManager.SaveScene(EditorSceneManager.GetActiveScene(), scenePath);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(scenePath, true) };
        Debug.Log("Saved " + scenePath + ". Press Play.");
    }

    static GameObject MakeObstaclePrefab(string name, Sprite sprite)
    {
        float h = 1.3f;
        float scale = h / sprite.bounds.size.y;
        var go = new GameObject(name);
        var sr = go.AddComponent<SpriteRenderer>();
        sr.sprite = sprite;
        sr.sortingOrder = 4;
        go.transform.localScale = Vector3.one * scale;
        go.transform.position = new Vector3(0, GroundY + h / 2f, 0);
        var box = go.AddComponent<BoxCollider2D>();
        box.size *= 0.8f;
        box.isTrigger = true;
        go.AddComponent<Obstacle>();
        var prefab = PrefabUtility.SaveAsPrefabAsset(go, "Assets/Prefabs/" + name + ".prefab");
        Object.DestroyImmediate(go);
        return prefab;
    }

    // Finds a texture by name ignoring case, spaces and underscores, and makes sure it is imported as a Sprite.
    static Sprite LoadSprite(string name)
    {
        string wanted = Normalize(name);
        foreach (string guid in AssetDatabase.FindAssets("t:Texture2D"))
        {
            string path = AssetDatabase.GUIDToAssetPath(guid);
            if (Normalize(Path.GetFileNameWithoutExtension(path)) != wanted) continue;

            var imp = AssetImporter.GetAtPath(path) as TextureImporter;
            if (imp != null && imp.textureType != TextureImporterType.Sprite)
            {
                imp.textureType = TextureImporterType.Sprite;
                imp.spriteImportMode = SpriteImportMode.Single;
                imp.SaveAndReimport();
            }
            var s = AssetDatabase.LoadAssetAtPath<Sprite>(path);
            if (s != null) return s;
        }
        return null;
    }

    static string Normalize(string s)
    {
        return s.Replace(" ", "").Replace("_", "").ToLowerInvariant();
    }
}
