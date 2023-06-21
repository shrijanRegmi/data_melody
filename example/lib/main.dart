import 'package:data_melody/data_melody.dart';
import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:flutter/material.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DataMelody.initialize();

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _dataMelody = DataMelody();
  late Stream<Map<String, dynamic>> _receivedDataStream;
  final _messageController = TextEditingController(text: 'hello world');
  var _selectedPlayerType = DataMelodyPlayer.ultrasonicNormal;

  @override
  void initState() {
    super.initState();
    _receivedDataStream = _dataMelody.receivedData;
    _startListening();
  }

  void _startListening() async {
    await _dataMelody.startReceivingData();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _inputBuilder(),
                const SizedBox(
                  height: 20.0,
                ),
                _actionsBuilder(),
                const SizedBox(
                  height: 50.0,
                ),
                _receivedDataBuilder(),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _inputBuilder() {
    return TextFormField(
      controller: _messageController,
      decoration: const InputDecoration(
        border: OutlineInputBorder(),
        labelText: 'Message',
      ),
    );
  }

  Widget _actionsBuilder() {
    return Row(
      children: [
        Expanded(
          child: DropdownButton<DataMelodyPlayer>(
            value: _selectedPlayerType,
            iconEnabledColor: Colors.blue,
            items: DataMelodyPlayer.values.map((e) {
              return DropdownMenuItem<DataMelodyPlayer>(
                value: e,
                child: Text(
                  e.name,
                  style: TextStyle(
                    color: _selectedPlayerType == e ? Colors.blue : null,
                  ),
                ),
              );
            }).toList(),
            onChanged: (val) {
              if (val != null) {
                setState(() {
                  _selectedPlayerType = val;
                });
              }
            },
          ),
        ),
        const SizedBox(
          width: 20.0,
        ),
        Expanded(
          child: ElevatedButton(
            child: const Text('Start Playing Sound'),
            onPressed: () async {
              await _dataMelody.startSendingData(
                data: _messageController.text,
                player: _selectedPlayerType,
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _receivedDataBuilder() {
    return StreamBuilder<Map<String, dynamic>>(
      stream: _receivedDataStream,
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          final data = snapshot.data!;
          return Text('Received data: $data');
        }

        return const Text('Nothing Received');
      },
    );
  }
}
