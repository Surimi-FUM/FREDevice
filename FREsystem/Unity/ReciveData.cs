using System;
using UnityEngine;
using System.Collections;
using System.Linq;
using JetBrains.Annotations;
using UnityEditor;
using WebSocketSharp;
 
[RequireComponent(typeof(Rigidbody))]
public class ReciveData : MonoBehaviour {
    private WebSocket _ws;
    private float _positionX = 900.0f;
    private float _positionZ = 500.0f;
    private string[] _message;
    private Rigidbody _rigidbody;
    private Vector3 _moveTo;
    private bool _beRay = false;
    // 位置座標
    private Vector3 position;
    // スクリーン座標をワールド座標に変換した位置座標
    private Vector3 screenToWorldPointPosition;
    
    private float mouse_move_x = 0f;
    private float mouse_move_y = 0f;
    private float sensitivity = 30.0f; // いわゆるマウス感度

    // Use this for initialization
    private void Start ()
    {
        //WebSocketの設定
        _ws = new WebSocket("ws://192.168.0.4:8124");
        _ws.Connect();
        _ws.Send("u");
        
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
            _message = e.Data.Split(' ');
            if (_message.Last() == "server") {Debug.Log(e.Data);}
            else
            {
                //デバック用
                Debug.Log(e.Data);
                _message = e.Data.Split(',');
                _positionX = float.Parse(_message.First());
                _positionZ = float.Parse(_message.Last());
            }
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
        //デバック用
        Debug.Log("Hit");
    }

    private void OnCollisionExit(Collision other)
    {
        _ws.Send("Off");
        //デバック用
        Debug.Log("Off");
    }
}
