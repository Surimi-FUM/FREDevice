using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class HandController : MonoBehaviour
{
    [SerializeField] private GameObject hand;
    [SerializeField] private float speed = 1.0f;
    private GraphAttributeDataForHand _graphAttributeData;
    private Transform _transform;
    
    // Start is called before the first frame update
    void Start()
    {
        _graphAttributeData = hand.GetComponent<GraphAttributeDataForHand>();
        _transform = transform;
    }

    // Update is called once per frame
    void Update()
    {
        _transform.rotation = Quaternion.Euler(_graphAttributeData.degree);
        if (_graphAttributeData.accel.sqrMagnitude < 1)
        {
            _graphAttributeData.accel.Normalize();
        }

        _graphAttributeData.accel *= Time.deltaTime;
        //_transform.Translate(_graphAttributeData.accel * speed);
    }
}
