using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using Google.Cloud.Firestore.V1;
using Grpc.Auth;
using System.Collections.ObjectModel;
using System.Diagnostics;
using YogaCustomer.Models;

namespace YogaCustomer.Services
{
    public class FirestoreService
    {
        private FirestoreDb _firestoreDb;

        public FirestoreService()
        {
            try
            {
                var assembly = typeof(FirestoreService).Assembly;
                using (Stream stream = assembly.GetManifestResourceStream("YogaCustomer.Resources.Raw.admin-sdk.json"))
                {
                    if (stream == null)
                    {
                        throw new FileNotFoundException("Admin sdk JSON file not found in embedded resources.");
                    }

                    GoogleCredential googleCredential = GoogleCredential.FromStream(stream);
                    FirestoreClient client = new FirestoreClientBuilder
                    {
                        ChannelCredentials = googleCredential.ToChannelCredentials()
                    }.Build();

                    _firestoreDb = FirestoreDb.Create("yoga-admin-ea96f", client);
                }
            }
            catch (Exception ex)
            {
                // Log the exception details
                Debug.WriteLine($"Error initializing FirestoreService: {ex.Message}");
                Debug.WriteLine($"Stack Trace: {ex.StackTrace}");

                throw;
            }
        }

        public void ListenForYogaClasses(Action<ObservableCollection<YogaClass>> onUpdate)
        {
            Query yogaClassQuery = _firestoreDb.CollectionGroup("yoga_classes");
            yogaClassQuery.Listen(async yogaClassesSnapshot =>
            {
                // Create a temporary observable collection to hold the yoga classes
                var updatedClasses = new ObservableCollection<YogaClass>();

                foreach (var yogaClassDocument in yogaClassesSnapshot.Documents)
                {
                    if (yogaClassDocument.Exists)
                    {
                        Dictionary<string, object> yogaClassData = yogaClassDocument.ToDictionary();
                        YogaClass yogaClass = new YogaClass
                        {
                            Id = yogaClassData.ContainsKey("id") && yogaClassData["id"] is long id ? (int)id : 0,
                            CourseId = yogaClassData.ContainsKey("courseId") && yogaClassData["courseId"] is long courseId ? (int)courseId : 0,
                            Teacher = yogaClassData.ContainsKey("teacher") ? yogaClassData["teacher"] as string : null,
                            Comments = yogaClassData.ContainsKey("comments") ? yogaClassData["comments"] as string : null,
                            Date = yogaClassData.ContainsKey("date") ? yogaClassData["date"] as string : null
                        };

                        var courseDoc = await _firestoreDb.Collection("courses").Document(yogaClass.CourseId.ToString()).GetSnapshotAsync();
                        if (courseDoc.Exists)
                        {
                            var courseData = courseDoc.ToDictionary();
                            yogaClass.CourseName = courseData.ContainsKey("name") ? courseData["name"] as string : null;
                            yogaClass.Price = courseData.ContainsKey("price") ? Convert.ToDouble(courseData["price"]) : 0.0;
                            yogaClass.Time = courseData.ContainsKey("time") ? courseData["time"] as string : null;
                        }

                        updatedClasses.Add(yogaClass);
                    }
                }

                // Call the update action with the updated observable collection of yoga classes
                onUpdate(updatedClasses);
            });
        }
    }
}
