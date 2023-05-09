using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

[RequireComponent(typeof(Rigidbody))]
public class HandHoseiController : MonoBehaviour
{
    [SerializeField] Text _dgreeText;
    [SerializeField] Text _accelText;
    [SerializeField] private GameObject hand;
    private GraphAttributeDataForHand _graphAttributeData;
    private Transform _transform;
    private Vector3 _rotation;
    private Text _text;
    private string[] message;
    
    // Start is called before the first frame update
    void Start()
    {
        _graphAttributeData = hand.GetComponent<GraphAttributeDataForHand>();
        _transform = transform;
    }

    // Update is called once per frame
    void Update()
    {
        if (_graphAttributeData.flag)
        {
            message = _graphAttributeData.message;
            _dgreeText.text = "Madqwic Degree[X , Y] : " + message[2] + " , " + message[3];
            //_accelText.text = "Accel[X , Y , Z] : " + message[3] + " , " + message[4] + " , " + message[5];

            _rotation = new Vector3(float.Parse(message[2]), 0f, -float.Parse(message[3]));
            _transform.rotation = Quaternion.Euler(_rotation);

        }
    }
}
