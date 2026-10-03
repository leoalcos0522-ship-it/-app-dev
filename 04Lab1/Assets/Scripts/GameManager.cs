using UnityEngine;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance { get; private set; }

    public float baseSpeed = 6f;
    public float maxSpeed = 10f;
    public float speedGain = 0.1f;   // speed added per second survived

    public bool IsOver { get; private set; }
    public float Speed { get; private set; }

    float score;
    int best;
    GUIStyle style;

    void Awake()
    {
        Instance = this;
        Time.timeScale = 1f;
        Speed = baseSpeed;
        best = PlayerPrefs.GetInt("Play2Best", 0);
    }

    void Update()
    {
        if (IsOver)
        {
            if (GameInput.JumpPressed())
            {
                Time.timeScale = 1f;
                SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
            }
            return;
        }

        score += Time.deltaTime * 10f;
        Speed = Mathf.Min(maxSpeed, baseSpeed + score / 10f * speedGain);
    }

    public void GameOver()
    {
        if (IsOver) return;
        IsOver = true;
        Time.timeScale = 0f;
        if ((int)score > best)
        {
            best = (int)score;
            PlayerPrefs.SetInt("Play2Best", best);
        }
    }

    void OnGUI()
    {
        if (style == null)
        {
            style = new GUIStyle(GUI.skin.label) { alignment = TextAnchor.UpperCenter, fontStyle = FontStyle.Bold };
            style.normal.textColor = Color.white;
        }
        style.fontSize = Mathf.Max(14, Screen.height / 20);

        GUI.Label(new Rect(0, 10, Screen.width, Screen.height / 10f),
            "Score: " + (int)score + "   Best: " + best, style);

        if (IsOver)
        {
            GUI.Label(new Rect(0, Screen.height * 0.35f, Screen.width, Screen.height * 0.3f),
                "GAME OVER\nPress SPACE / click / tap to restart", style);
        }
    }
}
