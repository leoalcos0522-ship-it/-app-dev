using UnityEngine;

public class ScrollingBackground : MonoBehaviour
{
    public float tileWidth = 20f;
    public int tileCount = 2;
    public float speedFactor = 0.3f;

    float halfCameraWidth = 12f;

    void Start()
    {
        Camera cam = Camera.main;
        if (cam != null) halfCameraWidth = cam.orthographicSize * cam.aspect;
    }

    void Update()
    {
        transform.Translate(Vector3.left * GameManager.Instance.Speed * speedFactor * Time.deltaTime, Space.World);

        // once a tile is fully off the left edge, jump it to the end of the row
        if (transform.position.x + tileWidth * 0.5f < -halfCameraWidth)
            transform.Translate(Vector3.right * tileWidth * tileCount, Space.World);
    }
}
