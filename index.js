import React, { useEffect, useRef, useCallback, forwardRef, useImperativeHandle } from 'react'
import { requireNativeComponent, UIManager, findNodeHandle, AppState, Platform, ViewPropTypes } from 'react-native'
import PropTypes from 'prop-types'

export const GstState = {
    VOID_PENDING: 0,
    NULL: 1,
    READY: 2,
    PAUSED: 3,
    PLAYING: 4
}

const GstPlayer = forwardRef((props, ref) => {
    const playerViewRef = useRef(null)
    const playerHandleRef = useRef(null)
    const appStateRef = useRef('active')
    const currentGstStateRef = useRef(undefined)
    const isInitializedRef = useRef(false)

    const setGstState = useCallback((state) => {
        const handle = playerHandleRef.current
        if (!handle) {
            return
        }

        UIManager.dispatchViewManagerCommand(
            handle,
            UIManager.RCTGstPlayer.Commands.setState,
            [state]
        )
    }, [])

    const recreateView = useCallback(() => {
        const handle = playerHandleRef.current
        if (!handle) {
            return
        }

        UIManager.dispatchViewManagerCommand(
            handle,
            UIManager.RCTGstPlayer.Commands.recreateView,
            []
        )
    }, [])

    const play = useCallback(() => setGstState(GstState.PLAYING), [setGstState])
    const pause = useCallback(() => setGstState(GstState.PAUSED), [setGstState])
    const stop = useCallback(() => setGstState(GstState.READY), [setGstState])

    const appStateChanged = useCallback((nextAppState) => {
        if (appStateRef.current.match(/inactive|background/) && nextAppState === 'active') {
            if (Platform.OS === 'ios') {
                recreateView()
            }
            play()
        } else {
            stop()
        }
        appStateRef.current = nextAppState
    }, [play, stop, recreateView])

    const onPlayerInit = useCallback(() => {
        isInitializedRef.current = true
        if (props.onPlayerInit) {
            props.onPlayerInit()
        }
    }, [props.onPlayerInit])

    const onStateChanged = useCallback((_message) => {
        const { old_state, new_state } = _message.nativeEvent
        currentGstStateRef.current = new_state

        if (old_state === GstState.PAUSED && new_state === GstState.READY) {
            if (Platform.OS === 'ios') {
                recreateView()
            }
        }

        if (props.onStateChanged) {
            props.onStateChanged(old_state, new_state)
        }
    }, [props.onStateChanged, recreateView])

    const onVolumeChanged = useCallback((_message) => {
        const { rms, peak, decay } = _message.nativeEvent
        if (props.onVolumeChanged) {
            props.onVolumeChanged(rms, peak, decay)
        }
    }, [props.onVolumeChanged])

    const onUriChanged = useCallback((_message) => {
        const { new_uri } = _message.nativeEvent

        if (props.onUriChanged) {
            props.onUriChanged(new_uri)
        }

        if (props.autoPlay) {
            play()
        }
    }, [props.onUriChanged, props.autoPlay, play])

    const onEOS = useCallback(() => {
        if (props.onEOS) {
            props.onEOS()
        }
    }, [props.onEOS])

    const onElementError = useCallback((_message) => {
        const { source, message, debug_info } = _message.nativeEvent
        if (props.onElementError) {
            props.onElementError(source, message, debug_info)
        }
    }, [props.onElementError])

    useImperativeHandle(ref, () => ({
        setGstState,
        play,
        pause,
        stop,
        recreateView
    }), [setGstState, play, pause, stop, recreateView])

    useEffect(() => {
        playerHandleRef.current = findNodeHandle(playerViewRef.current)

        const subscription = AppState.addEventListener?.('change', appStateChanged)

        return () => {
            if (subscription && typeof subscription.remove === 'function') {
                subscription.remove()
            } else {
                AppState.removeEventListener('change', appStateChanged)
            }
        }
    }, [appStateChanged])

    return (
        <RCTGstPlayer
            autoPlay={props.autoPlay}
            uri={props.uri || undefined}
            audioLevelRefreshRate={props.audioLevelRefreshRate !== undefined ? props.audioLevelRefreshRate : 100}
            isDebugging={props.isDebugging !== undefined ? props.isDebugging : false}

            onPlayerInit={onPlayerInit}
            onStateChanged={onStateChanged}
            onVolumeChanged={onVolumeChanged}
            onUriChanged={onUriChanged}
            onEOS={onEOS}
            onElementError={onElementError}

            ref={playerViewRef}
            {...props}
        />
    )
})

GstPlayer.propTypes = {
    uri: PropTypes.string,
    autoPlay: PropTypes.bool,
    audioLevelRefreshRate: PropTypes.number,
    isDebugging: PropTypes.bool,

    onPlayerInit: PropTypes.func,
    onStateChanged: PropTypes.func,
    onVolumeChanged: PropTypes.func,
    onUriChanged: PropTypes.func,
    onEOS: PropTypes.func,
    onElementError: PropTypes.func,

    setGstState: PropTypes.func,
    play: PropTypes.func,
    pause: PropTypes.func,
    stop: PropTypes.func,

    createDrawableSurface: PropTypes.func,
    destroyDrawableSurface: PropTypes.func,
    recreateView: PropTypes.func,

    ...ViewPropTypes
}

export default GstPlayer

const RCTGstPlayer = requireNativeComponent('RCTGstPlayer', GstPlayer, {
    nativeOnly: { onChange: true }
})
