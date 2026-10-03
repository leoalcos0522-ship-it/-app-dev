using UnityEngine;

public class Obstacle : MonoBehaviour
{
    public float despawnX = -14f;

    void Update()
    {
        transform.Translate(Vector3.left * GameManager.Instance.Speed * Time.deltaTime, Space.World);
        if (transform.position.x < despawnX) Destroy(gameObject);
    }
}
