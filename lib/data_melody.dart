import 'package:data_melody/data_melody_platform_interface.dart';
import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:permission_handler/permission_handler.dart';

class DataMelody {
  /// Initializes the data melody SDK. This is usually done before the runApp call.
  ///
  /// Example:
  /// ```dart
  /// void main() async {
  ///   await DataMelody.initialize();
  ///   runApp(const MyApp());
  /// }
  /// ```
  static Future<void> initialize() {
    return DataMelodyPlatform.instance.initialize();
  }

  /// Converts the provided data to sound and plays the native audio player.
  ///
  /// Specify the [data] parameter as the data to send through sound.
  /// The [data] parameter must not be null.
  ///
  /// Example:
  /// ```dart
  ///    final _dataMelody = DataMelody();
  ///
  ///    _dataMelody.startSendingData(
  ///       data: "Hello World",
  ///    );
  /// ```
  Future<void> startSendingData({
    required final String data,
    final DataMelodyPlayer player = DataMelodyPlayer.ultrasonicNormal,
    final int volume = 50,
  }) {
    return DataMelodyPlatform.instance.startSendingData(
      data: data,
      player: player,
      volume: volume,
    );
  }

  /// Stops the native audio player, if it is playing.
  ///
  /// Example:
  /// ```dart
  ///    final _dataMelody = DataMelody();
  ///
  ///     await _dataMelody.stopSendingData();
  /// ```
  Future<void> stopSendingData() {
    return DataMelodyPlatform.instance.stopSendingData();
  }

  /// Starts the native audio recorder to capture the sound played from other devices.
  ///
  /// Example:
  /// ```dart
  ///    final _dataMelody = DataMelody();
  ///
  ///     await _dataMelody.startReceivingData();
  /// ```
  Future<void> startReceivingData() {
    return DataMelodyPlatform.instance.startReceivingData();
  }

  /// Stops the native audio recorder, if it is recording.
  ///
  /// Example:
  /// ```dart
  ///    final _dataMelody = DataMelody();
  ///
  ///     await _dataMelody.stopReceivingData();
  /// ```
  Future<void> stopReceivingData() {
    return DataMelodyPlatform.instance.stopReceivingData();
  }

  /// Get the stream of data that is received from the native audio recorder. The [startReceivingData] function must me called before this getter.
  Stream<Map<String, dynamic>> get receivedData =>
      DataMelodyPlatform.instance.receivedData;

  Future<PermissionStatus> requestReceivingPermission() {
    return DataMelodyPlatform.instance.requestReceivingPermission();
  }
}
