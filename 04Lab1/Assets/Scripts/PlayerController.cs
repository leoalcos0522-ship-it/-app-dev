using UnityEngine;

[RequireComponent(typeof(Rigidbody2D))]
public class PlayerController : MonoBehaviour
{
    public float jumpVelocity = 13f;

    Rigidbody2D rb;
    bool grounded;

    void Awake()
    {
        rb = GetComponent<Rigidbody2D>();
    }

    void Update()
    {
        if (GameManager.Instance != null && GameManager.Instance.IsOver) return;

        if (grounded && GameInput.JumpPressed())
        {
#if UNITY_6000_0_OR_NEWER
            rb.linearVelocity = new Vector2(0f, jumpVelocity);
#else
            rb.velocity = new Vector2(0f, jumpVelocity);
#endif
            grounded = false;
        }
    }

    // Only the ground is solid, so any upward-facing contact means we are standing.
    void OnCollisionStay2D(Collision2D c)
    {
        foreach (ContactPoint2D p in c.contacts)
        {
            if (p.normal.y > 0.5f) { grounded = true; return; }
        }
    }

    void OnCollisionExit2D(Collision2D c)
    {
        grounded = false;
    }

    void OnTriggerEnter2D(Collider2D other)
    {
        if (other.GetComponent<Obstacle>() != null)
            GameManager.Instance.GameOver();
    }
}
