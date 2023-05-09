using UnityEngine;
using UnityEngine.UI;

public class GraphAttributeDataForHand : MonoBehaviour
{
    [SerializeField] Text _dgreeText;
    [SerializeField] Text _accelText;
    [SerializeField] private Text sensortext;
    private SerialData _serialData;
    public string[] message;
    private int sample = 0;
    private float time = 0;
    public bool flag = false;

    [Graph(100)] public Vector3 degree = Vector3.zero;
    [Graph(100)] public Vector3 accel = Vector3.zero;
    
    void Start()
    {
        Application.runInBackground = true;
    }

    void Update()
    {
        if (sensortext.text != null)
        {
            flag = true;
            message = sensortext.text.Split(',');
            _dgreeText.text = "Gyro Degree[X , Y] : " + message[0] + " , " + message[1];
            //_accelText.text = "Accel[X , Y , Z] : " + message[3] + " , " + message[4] + " , " + message[5];
            degree = new Vector3(float.Parse(message[0]), 0f, -float.Parse(message[1]));
            //accel = new Vector3(float.Parse(message[3]), 0f, float.Parse(message[4]));
        }
    }
}