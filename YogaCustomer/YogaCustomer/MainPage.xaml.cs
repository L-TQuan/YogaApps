using System.Collections.ObjectModel;
using System.Globalization;
using YogaCustomer.Models;
using YogaCustomer.Services;

namespace YogaCustomer
{
    public partial class MainPage : ContentPage
    {
        private FirestoreService _firestoreService;
        private ObservableCollection<YogaClass> _allYogaClasses;

        public MainPage()
        {
            InitializeComponent();
            _firestoreService = new FirestoreService();
            _allYogaClasses = new ObservableCollection<YogaClass>(); // Initialize the ObservableCollection
            YogaClassListView.ItemsSource = _allYogaClasses;

            LoadYogaClasses();
        }

        private void OnCancelSelectionClicked(object sender, EventArgs e)
        {
            // Reset all fields to their default state
            DateEntry.Text = string.Empty;
            DayPicker.SelectedIndex = -1;
            TimePicker.Time = TimeSpan.FromHours(0);

            DisplayAlert("Selection Reset", "All search fields have been cleared.", "OK");
        }

        private void LoadYogaClasses()
        {
            _firestoreService.ListenForYogaClasses(yogaClasses =>
            {
                // Clear existing classes
                _allYogaClasses.Clear();

                // Add new classes to the ObservableCollection
                foreach (var yogaClass in yogaClasses)
                {
                    _allYogaClasses.Add(yogaClass);
                }

                // Update the UI on the main thread
                MainThread.BeginInvokeOnMainThread(() =>
                {
                    YogaClassListView.ItemsSource = _allYogaClasses; // Set to the updated ObservableCollection
                });
            });
        }


        private void OnSearchClicked(object sender, EventArgs e)
        {
            string dateInput = DateEntry.Text;
            string selectedDay = DayPicker.SelectedItem?.ToString();
            TimeSpan? selectedTime = TimePicker.Time != TimeSpan.FromHours(0) ? (TimeSpan?)TimePicker.Time : null;

            // Check if _allYogaClasses is initialized
            if (_allYogaClasses == null || !_allYogaClasses.Any())
            {
                DisplayAlert("Error", "Yoga classes data is not available.", "OK");
                return;
            }

            // Start with all yoga classes
            var filteredClasses = new List<YogaClass>(_allYogaClasses);

            // Filter by date if provided
            if (!string.IsNullOrEmpty(dateInput))
            {
                if (DateTime.TryParseExact(dateInput, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime date))
                {
                    // Filter classes by the input date
                    filteredClasses = filteredClasses
                        .Where(yogaClass =>
                            DateTime.TryParseExact(yogaClass.Date, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime classDate) &&
                            classDate.Date == date.Date)
                        .ToList();
                }
                else
                {
                    DisplayAlert("Invalid Date", "Please enter a valid date in yyyy-mm-dd format.", "OK");
                    return;
                }
            }

            // Filter by day of the week if selected
            if (!string.IsNullOrEmpty(selectedDay))
            {
                if (Enum.TryParse(typeof(DayOfWeek), selectedDay, out var dayOfWeek))
                {
                    // Further filter classes by the selected day of the week
                    filteredClasses = filteredClasses
                        .Where(yogaClass =>
                            DateTime.TryParseExact(yogaClass.Date, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime classDate) &&
                            classDate.DayOfWeek == (DayOfWeek)dayOfWeek)
                        .ToList();
                }
            }

            // Filter by time if provided
            if (selectedTime.HasValue)
            {
                // Further filter classes by the specified time
                filteredClasses = filteredClasses
                    .Where(yogaClass =>
                        !string.IsNullOrEmpty(yogaClass.Time) && // Ensure there's a valid time string
                        TimeSpan.TryParse(yogaClass.Time, out TimeSpan classTime) && // Convert yoga class time to TimeSpan
                        classTime == selectedTime.Value) // Compare times
                    .ToList();
            }


            // Update the UI with filtered results
            MainThread.BeginInvokeOnMainThread(() =>
            {
                YogaClassListView.ItemsSource = filteredClasses;
                if (filteredClasses.Count == 0)
                {
                    DisplayAlert("No Results", "No yoga classes found matching your criteria.", "OK");
                }
            });
        }
    }
}

