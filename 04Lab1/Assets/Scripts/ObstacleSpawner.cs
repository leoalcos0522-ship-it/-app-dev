using UnityEngine;

public class ObstacleSpawner : MonoBehaviour
{
    public GameObject[] prefabs;
    public float spawnX = 12f;
    public float minGap = 10f;   // world units between obstacles
    public float maxGap = 16f;

    float timer = 1.2f;

    void Update()
    {
        if (prefabs == null || prefabs.Length == 0) return;

        timer -= Time.deltaTime;
        if (timer > 0f) return;

        GameObject prefab = prefabs[Random.Range(0, prefabs.Length)];
        Instantiate(prefab, new Vector3(spawnX, prefab.transform.position.y, 0f), Quaternion.identity);

        // gap is in distance, so the time between spawns shrinks as the game speeds up
        timer = Random.Range(minGap, maxGap) / GameManager.Instance.Speed;
    }
}
