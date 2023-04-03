using UnityEngine;
using WebSocketSharp;
 
[RequireComponent(typeof(Rigidbody))]
public class WebSocketFinger : MonoBehaviour {
    private WebSocket _ws;
    private string[] _message;
    private Rigidbody _rigidbody;

    private float mouse_move_x = 0f;
    private float mouse_move_y = 0f;
    private float sensitivity = 30.0f; // いわゆるマウス感度

    // Use this for initialization
    private void Start ()
    {
        //WebSocketの設定
        _ws = new WebSocket("ws://192.168.0.4:8124");
        _ws.Connect();
        
        _ws.OnOpen += (sender, e) =>
        {
            Debug.Log("WebSocket Open");
            
        };

        _ws.OnError += (sender, e) =>
        {
            Debug.Log("WebSocket Error Message: " + e.Message);
        };

        _ws.OnClose += (sender, e) =>
        {
            Debug.Log("WebSocket Close");
        };
        
        _ws.OnMessage += (object sender, MessageEventArgs e) =>
        {
            Debug.Log(e.Data);
        };

        //_controller = GetComponent<CharacterController>();
        // 回転しない、位置 Y だけ固定
        _rigidbody = this.GetComponent<Rigidbody>();
        _rigidbody.constraints = RigidbodyConstraints.FreezeRotation | RigidbodyConstraints.FreezePositionY;
    }
     
    // Update is called once per frame
    private void Update()
    {
        if (Input.GetKey(KeyCode.Escape)) Quit();
        
        mouse_move_x = Input.GetAxis("Mouse X") * sensitivity;
        mouse_move_y = Input.GetAxis("Mouse Y") * sensitivity;
    }

    private void FixedUpdate()
    {
        _rigidbody.velocity = new Vector3 (mouse_move_x, 0, mouse_move_y);
    }

    private void Quit()
    {
        _ws.Close();
        UnityEditor.EditorApplication.isPlaying = false;
    }

    private void OnCollisionEnter(Collision other)
    {
        _ws.Send("Hit");
    }

    private void OnCollisionExit(Collision other)
    {
        _ws.Send("Off");
    }
}
