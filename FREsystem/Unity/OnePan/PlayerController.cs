using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityStandardAssets.CrossPlatformInput;

[RequireComponent(typeof(Animator))]
public class PlayerController : MonoBehaviour
{
    private Animator _animator;
    private Collider righthandCollider;
    private GraphAttributeDataForUnitychan _graphAttributeData;
    private bool Onpanch = false;

    // Start is called before the first frame update
    void Start()
    {
        _animator = GetComponent<Animator>();
        righthandCollider = GameObject.Find("Character1_RightHand").GetComponent<SphereCollider>();
        _graphAttributeData = GetComponent<GraphAttributeDataForUnitychan>();
    }

    // Update is called once per frame
    void Update()
    {
        if (!Onpanch)
        {
            if (_graphAttributeData.accel.x > 1.5f || CrossPlatformInputManager.GetButton("Fire1"))
            {
                _animator.SetTrigger("Attack");
                //左手コライダーをオンにする
                righthandCollider.enabled = true;
                Onpanch = true;
                Invoke("OnPunchFinish", 0.5f);
                Invoke("RePanch", 0.8f);
            }
        }
    }

    private void OnPunchFinish()
    {
        _animator.SetTrigger("Normal");
        righthandCollider.enabled = false;
    }

    private void RePanch()
    {
        Onpanch = false;
    }
}
