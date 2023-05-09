using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class SerialData : MonoBehaviour
{
    public SerialHandler serialHandler;
    public Text _text;
    private bool first_m = true;
    private float time = 0.0f;

    // Use this for initialization
    void Start () {
        //信号を受信したときに、そのメッセージの処理を行う
        serialHandler.OnDataReceived += OnDataReceived;
    }
	
    // Update is called once per frame
    void Update () {
		
    }

    /*
     * シリアルを受け取った時の処理
     */
    void OnDataReceived(string m)
    {
        try
        {
            if (first_m)
            {
                //最初の文字列を無視する
                first_m = false;
            }
            else
            {
                _text.text = m;
                time += Time.deltaTime;
            }
        }
        catch (System.Exception e)
        {
            Debug.LogWarning(e.Message);
        }
    }
}
